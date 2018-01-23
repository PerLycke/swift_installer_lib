package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.UserHandle
import com.brit.swiftinstaller.utils.constants.CURRENT_USER


object Utils {
    fun getOverlayPackageName(pack: String): String {
        return pack + ".swiftinstaller.overlay";
    }

    fun isOverlayInstalled(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    fun isOverlayEnabled(context: Context, packageName: String): Boolean {
        return runCommand("cmd overlay").output!!.contains("packageName")
    }
}