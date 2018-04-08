package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

class UpdateChecker() {

    companion object {
        fun checkForOverlayUpdates(context: Context) {
            val available = ArrayList<String>()
            for (packageName in context.assets.list("overlays")) {
                if (Utils.isOverlayInstalled(context, Utils.getOverlayPackageName(packageName))) {
                    val aInfo = context.packageManager.getApplicationInfo(Utils.getOverlayPackageName(packageName), PackageManager.GET_META_DATA)
                    val ver = ShellUtils.inputStreamToString(context.assets.open("overlays/$packageName/version"))
                    val version = Integer.parseInt(ver.trim().replace("\"", ""))
                    val current = aInfo.metaData.getInt("overlay_version")
                    if (current < version) {
                        addAppToUpdate(context, packageName)
                    }
                }
            }
        }
    }
}