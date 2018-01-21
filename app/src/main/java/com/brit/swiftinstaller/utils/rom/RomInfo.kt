package com.brit.swiftinstaller.utils.rom

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.Log

import com.brit.swiftinstaller.ui.activities.MainActivity

import java.io.File

import com.brit.swiftinstaller.utils.getProperty

class RomInfo internal constructor(var name: String, var version: String, vararg vars: String) {
    private val overlayFolder: String? = null

    val variants = vars

    //TODO expand installer
    val isSamsung: Boolean
        get() = true

    fun preInstall(context: Context, themePackage: String) {
        //TODO
    }

    fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        //TODO
    }

    fun postInstall(context: Context, targetPackage: String) {
        //TODO
    }

    fun uninstall(context: Context, packageName: String) {
        //TODO
    }

    fun createFinishedDialog(activity: MainActivity): Dialog {
        //TODO
        return AlertDialog.Builder(activity)
                .setMessage("Would you like to reboot?")
                .setPositiveButton(android.R.string.ok
                ) { dialog, which -> }
                .setNegativeButton("Later") { dialog, which -> }.create()
    }

    fun isOverlayCompatible(packageName: String): Boolean {
        return true
    }

    companion object {

        private val TAG = "RomInfo"

        @JvmStatic private lateinit var sInfo: RomInfo

        @Synchronized @JvmStatic
        fun getRomInfo(context: Context): RomInfo {
            if (sInfo == null) {
                return sInfo
            }
            val oxygen = getProperty("ro.oxygen.version")
            sInfo = RomInfo("AOSP", Build.VERSION.RELEASE, "type3_v7_1")
            return sInfo
        }

        private val isTouchwiz: Boolean
            get() = File("/system/framework/touchwiz.jar").exists()

        private fun isOMS(context: Context): Boolean {
            val am = context.getSystemService(ActivityManager::class.java)!!
            val services = am.getRunningServices(Integer.MAX_VALUE)
            for (info in services) {
                Log.d("TEST", "name - " + info.service.className)
                if (info.service.className.contains("IOverlayManager")) {
                    return true
                }
            }
            return false
        }

        fun isSupported(context: Context): Boolean {
            return true
        }
    }
}
