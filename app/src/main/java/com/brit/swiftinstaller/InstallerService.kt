package com.brit.swiftinstaller

import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.brit.swiftinstaller.installer.OverlayManager
import com.brit.swiftinstaller.utils.AssetHelper.Companion.copyAssetFolder
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.constants.SIMULATE_INSTALL
import com.brit.swiftinstaller.utils.deleteFileShell
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.rom.RomInfo

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.Executors

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class InstallerService : Service() {

    private var mPackageName: String? = null

    private var mExecutor = Executors.newFixedThreadPool(1)

    private var mRomInfo: RomInfo? = null

    private var mCipher: Cipher? = null

    private var mOM: OverlayManager? = null

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
            }

            override fun startInstall(apps: List<String>) {
                install(apps)
            }

            override fun startUninstall(apps: List<String>) {
                uninstall(false, apps)
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

        mOM = OverlayManager(this)

        sService = IInstallerService.Stub.asInterface(onBind(intent))

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

    private fun uninstall(preInstall: Boolean, apps: List<String>) {
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
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return
        }
        for (packageName in apps) {
            mRomInfo!!.uninstallOverlay(this, packageName)
        }

    }

    private fun install(apps: List<String>) {
        if (mExecutor.isShutdown)
            mExecutor = Executors.newFixedThreadPool(1)

        // check for previous theme
        //uninstall(true, packageName)

        mRomInfo!!.preInstall(this, mPackageName!!)

        if (SIMULATE_INSTALL) {

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

                    mExecutor.submit {
                        try {
                            Thread.sleep(1000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
                mRomInfo!!.postInstall(this, mPackageName!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            mOM!!.compileOverlays(apps)
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
            copyAssetFolder(am, path, resDir.absolutePath, mCipher)
        }
        if (overlay.equals("android")) {
            applyAccent(resDir)
        }
        generateManifest(overlayDir.absolutePath,
                mPackageName, overlay!!, info.versionName)
        val overlayPath = cacheDir.toString() + "/" + mPackageName + "/overlays/" +
                mPackageName + "." + overlay + ".apk"
        Log.d("TEST", "compiling")
        ShellUtils.compileOverlay(this, mPackageName!!, resDir.absolutePath,
                overlayDir.absolutePath + "/AndroidManifest.xml",
                overlayPath, null, info.applicationInfo)
        Log.d("TEST", "compiled")
        mRomInfo!!.installOverlay(this, overlay, overlayPath)

        //ShellUtils.deleteFile(overlayPath);
        if (!deleteFileShell(overlayDir.absolutePath)) {
            Log.e(TAG, "Unable to delete " + overlayPath)
        }
    }

    private fun applyAccent(resDir: File) {
        val accent = getAccentColor(this)
        //val hightlightColor =
        val file = StringBuilder()
        file.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        file.append("<resources>\n")
        file.append("<color name=\"material_blue_grey_900\">#" + String.format("%06x", accent).substring(2) + "</color>")
        //file.append("<color name=\"highlighted_text_dark\">")
        file.append("</resources>")

        val values = File(resDir, "/values")
        values.mkdirs()
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter(resDir.absolutePath + "/values/accent.xml"))
            writer.write(file.toString())
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

    companion object {

        const val TAG = "InstallerService"

        const val ARG_THEME_PACKAGE = "package_name"

        const val ARG_ENCRYPTION_KEY = "encryption_key"
        const val ARG_IV_KEY = "iv_key"

        private lateinit var sService: IInstallerService

        fun getService() : IInstallerService {
            return sService
        }
    }
}
