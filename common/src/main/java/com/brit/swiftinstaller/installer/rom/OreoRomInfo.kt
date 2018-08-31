package com.brit.swiftinstaller.installer.rom

import android.content.Context
import android.content.Intent
import com.brit.swiftinstaller.ui.activities.CustomizeActivity
import com.brit.swiftinstaller.utils.*

open class OreoRomInfo(context: Context) : RomInfo(context) {

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
        if (ShellUtils.isRootAvailable && !uninstall) {
            if (apps.contains("android")) {
                apps.remove("android")
                apps.add("android")
            }
            for (app in apps) {
                runCommand("cmd overlay enable " + Utils.getOverlayPackageName(app), true)
            }
        }

        if (!uninstall && oppositeApps != null && oppositeApps.isNotEmpty()) {
            for (app in oppositeApps) {
                uninstallOverlay(context, app)
            }
        }

        if (intent != null) {
            context.applicationContext.startActivity(intent)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + Utils.getOverlayPackageName(packageName), true)
        }
    }

    override fun getCustomizeFeatures() : Int {
        return CustomizeActivity.SUPPORTS_SHADOW
    }

    override fun disableOverlay(targetPackage: String) {
        runCommand("cmd overlay disable ${Utils.getOverlayPackageName(targetPackage)}", true)
    }
}