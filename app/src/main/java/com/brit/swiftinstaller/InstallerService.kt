package com.brit.swiftinstaller

import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.util.ArraySet
import android.util.Log
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.constants.INSTALL_FAILED_INCOMPATIBLE
import com.brit.swiftinstaller.utils.constants.SIMULATE_INSTALL
import com.brit.swiftinstaller.utils.deleteFileShell
import com.brit.swiftinstaller.utils.rom.RomInfo

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Arrays

import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class InstallerService : Service() {

    private var mPackageName: String? = null
    private var mExcludedPackages: Set<String> = ArraySet()

    private var mCallback: IInstallerCallback? = null

    private var mRomInfo: RomInfo? = null

    private var mCipher: Cipher? = null

    private val themeAssets: AssetManager?
        get() {
            return try {
                val res = packageManager.getResourcesForApplication(mPackageName)
                res.assets
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

        }

    override fun onBind(intent: Intent): IBinder? {
        return object : IInstallerService.Stub() {

            @Throws(RemoteException::class)
            override fun setCallback(callback: IInstallerCallback) {
                mCallback = callback
            }

            override fun startInstall(apps: List<String>) {
                install(apps)
            }

            override fun startUninstall(app: String?) {
                uninstall(false, app)
            }

            @Throws(RemoteException::class)
            override fun updateApp(packageName: String?) {
                //mRomInfo!!.mountRW()
                val info: PackageInfo
                try {
                    info = packageManager.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    //mRomInfo!!.mountRO()
                    return
                }

                installApp(themeAssets, info, packageName)
                //mRomInfo!!.mountRO()
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mPackageName = intent.getStringExtra(ARG_THEME_PACKAGE)
        //mExcludedPackages = Settings.getExcludedApps(this)
        mRomInfo = RomInfo.getRomInfo(this)

        val enKey = intent.getByteArrayExtra(ARG_ENCRYPTION_KEY)
        val ivKey = intent.getByteArrayExtra(ARG_IV_KEY)

        try {
            mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            mCipher!!.init(Cipher.DECRYPT_MODE, SecretKeySpec(enKey, "AES"),
                    IvParameterSpec(ivKey))
        } catch (e: Exception) {
            mCipher = null
        }

        return Service.START_NOT_STICKY
    }

    private fun uninstall(preInstall: Boolean, packageName: String?) {
        if (!preInstall) {
            try {
                mCallback!!.installStarted()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        }
        if (SIMULATE_INSTALL) {
            val am = themeAssets ?: return
            try {
                val olays = am.list("overlays")
                val overlays = ArrayList<String>()
                overlays.addAll(Arrays.asList(*olays))
                for (overlay in overlays) {
                    val info: ApplicationInfo
                    try {
                        info = packageManager.getApplicationInfo(overlay, 0)
                    } catch (e: PackageManager.NameNotFoundException) {
                        continue
                    }

                    try {
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    try {
                        mCallback!!.progressUpdate(info.loadLabel(packageManager).toString(),
                                overlays.indexOf(overlay), overlays.size, true)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }

                }
                try {
                    if (preInstall) {
                        mCallback!!.installComplete(true)
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return
        }
        //mRomInfo!!.uninstall(this, mCallback, packageName)
        try {
            if (!preInstall) {
                mCallback!!.installComplete(true)
            }
        } catch (ignored: RemoteException) {
        }

    }

    private fun install(apps: List<String>) {

        try {
            mCallback!!.installStarted()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        // check for previous theme
        uninstall(true, packageName)

        mRomInfo!!.preInstall(this, mPackageName!!)

        val am = themeAssets ?: return
        try {
            for (overlay in apps) {
                val info: ApplicationInfo
                val pInfo: PackageInfo
                try {
                    info = packageManager.getApplicationInfo(overlay, 0)
                    pInfo = packageManager.getPackageInfo(overlay, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }

                try {
                    mCallback!!.progressUpdate(info.loadLabel(packageManager) as String,
                            apps.indexOf(overlay), apps.size, false)
                } catch (ignored: RemoteException) {
                }

                if (SIMULATE_INSTALL) {
                    try {
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    continue
                }
                installApp(am, pInfo, overlay)
            }
            mRomInfo!!.postInstall(this, mPackageName!!)
            try {
                mCallback!!.installComplete(false)
            } catch (ignored: RemoteException) {
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun installApp(am: AssetManager?, info: PackageInfo, overlay: String?) {
        val assetPaths = ArrayList<String>()
        assetPaths.add("overlays/$overlay/res")
        val ri = mRomInfo
        try {
            val variants = am!!.list("overlays/" + overlay)
            for (v in ri!!.variants) {
                if (variants != null && Arrays.asList(*variants).contains(v)) {
                    assetPaths.add("overlays/$overlay/$v")
                    break
                }
            }

        } catch (ignored: Exception) {
        }

        val overlayDir = File(cacheDir, "overlays/" + overlay)
        val resDir = File(overlayDir, "res")
        for (path in assetPaths) {
            copyAssetFolder(am, path, resDir.absolutePath)
        }
        generateManifest(overlayDir.absolutePath,
                mPackageName, overlay!!, info.versionName)
        val overlayPath = cacheDir.toString() + "/" + mPackageName + "/overlays/" +
                mPackageName + "." + overlay + ".apk"
        ShellUtils.compileOverlay(this, mPackageName!!, resDir.absolutePath,
                overlayDir.absolutePath + "/AndroidManifest.xml",
                overlayPath, null, info.applicationInfo)
        mRomInfo!!.installOverlay(this, overlay, overlayPath)

        //ShellUtils.deleteFile(overlayPath);
        if (!deleteFileShell(overlayDir.absolutePath)) {
            Log.e(TAG, "Unable to delete " + overlayPath)
        }
    }

    private fun generateManifest(path: String, packageName: String?,
                                 targetPackage: String, themeVersion: String) {
        val manifest = StringBuilder()
        manifest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        manifest.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        manifest.append("package=\"" + Utils.getOverlayPackageName(targetPackage) + "\">\n")
        manifest.append("<overlay ")
        manifest.append("android:priority=\"1\" ")
        if (targetPackage == "android") {
            manifest.append("android:targetPackage=\"fwk\"/>\n")
        } else {
            manifest.append("android:targetPackage=\"$targetPackage\"/>\n")
        }
        manifest.append("<application android:allowBackup=\"false\" android:hasCode=\"false\">\n")
        manifest.append("<meta-data android:name=\"theme_version\" android:value=\"v="
                + themeVersion + "\"/>\n")
        manifest.append("<meta-data android:name=\"theme_package\" android:value=\""
                + packageName + "\"/>\n")
        manifest.append("<meta-data android:name=\"target_package\" android:value=\""
                + targetPackage + "\"/>\n")
        manifest.append("</application>\n")
        manifest.append("</manifest>")

        Log.d("TEST", "manifest - " + manifest.toString())

        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter(path + "/AndroidManifest.xml"))
            writer.write(manifest.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (writer != null) {
                    writer.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun copyAssetFolder(am: AssetManager?, assetPath: String, path: String): Boolean {
        try {
            val files = am!!.list(assetPath)
            val f = File(path)
            if (!f.exists() && !f.mkdirs()) {
                throw RuntimeException("cannot create directory: " + path)
            }
            Log.d("TEST", "files - " + Arrays.toString(files))
            var res = true
            for (file in files) {
                res = if (am.list(assetPath + "/" + file).isEmpty()) {
                    res and copyAsset(am, assetPath + "/" + file, path + "/" + file)
                } else {
                    res and copyAssetFolder(am, assetPath + "/" + file, path + "/" + file)
                }
            }
            return res
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

    }

    private fun copyAsset(am: AssetManager?, assetPath: String, realPath: String): Boolean {
        var path = realPath
        var `in`: InputStream? = null
        var out: OutputStream? = null
        val parent = File(path).parentFile
        if (!parent.exists() && !parent.mkdirs()) {
            throw RuntimeException("cannot create directory: " + parent.absolutePath)
        }

        if (path.endsWith(".enc")) {
            path = path.substring(0, path.lastIndexOf("."))
        }

        Log.d("TEST", "assetPath - " + assetPath)

        try {
            `in` = if (mCipher != null && assetPath.endsWith(".enc")) {
                CipherInputStream(am!!.open(assetPath), mCipher)
            } else {
                am!!.open(assetPath)
            }
            out = FileOutputStream(File(path))
            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            var len: Int = `in`!!.read(bytes)
            while (len != -1) {
                out.write(bytes, 0, len)
                len = `in`.read(bytes)
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                if (`in` != null) {
                    `in`.close()
                }
                if (out != null) {
                    out.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        val TAG = "InstallerService"

        val ARG_THEME_PACKAGE = "package_name"
        val ARG_NOTIFICATION_ACCENT = "notification_color"

        val ARG_ENCRYPTION_KEY = "encryption_key"
        val ARG_IV_KEY = "iv_key"
    }
}
