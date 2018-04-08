package com.brit.swiftinstaller

import android.app.Service
import android.content.Intent
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
import java.util.*
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class InstallerService : Service() {

    private var mPackageName: String? = null

    private var mExecutor = Executors.newFixedThreadPool(1)

    private lateinit var mRomInfo: RomInfo
    private lateinit var mOM: OverlayManager


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
                mOM.setCallback(callback)
            }

            override fun startInstall(apps: List<String>) {
                install(apps)
            }

            override fun startUninstall(apps: List<String>) {
                Log.d("TEST", "InstallerService - uninstall")
                uninstall(apps)
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

                //installApp(themeAssets, info, packageName)
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

    private fun uninstall(apps: List<String>) {
        if (SIMULATE_INSTALL) {
            val am = themeAssets ?: return
            try {
                val olays = am.list("overlays")
                val overlays = ArrayList<String>()
                overlays.addAll(Arrays.asList(*olays))
                for (overlay in overlays) {
                    try {
                        packageManager.getApplicationInfo(overlay, 0)
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
        for (app in apps) {
            Log.d("TEST", "uninstalling app - $app")
        }
        mOM.uninstallOverlays(apps)
    }

    private fun install(apps: List<String>) {
        if (mExecutor.isShutdown)
            mExecutor = Executors.newFixedThreadPool(1)

        // check for previous theme
        //uninstall(true, packageName)

        mRomInfo.preInstall()

        if (SIMULATE_INSTALL) {

            try {
                for (overlay in apps) {
                    try {
                        packageManager.getApplicationInfo(overlay, 0)
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
                //mRomInfo.postInstall(this)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            mOM.installOverlays(apps)
        }

    }

    companion object {

        const val TAG = "InstallerService"

        const val ARG_THEME_PACKAGE = "package_name"

        const val ARG_ENCRYPTION_KEY = "encryption_key"
        const val ARG_IV_KEY = "iv_key"

        private lateinit var sService: IInstallerService

        fun getService(): IInstallerService {
            return sService
        }
    }
}
