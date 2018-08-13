package com.brit.swiftinstaller.installer.rom

import android.content.Context
import android.content.Intent
import com.brit.swiftinstaller.utils.*

class PRomInfo(context: Context) : RomInfo(context) {

    private val systemApp = "/system/app"

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        val overlayPackage = Utils.getOverlayPackageName(targetPackage)
        if (ShellUtils.isRootAvailable) {
            remountRW("/system")
            ShellUtils.mkdir("$systemApp/$overlayPackage")
            ShellUtils.copyFile(overlayPath, "$systemApp/$overlayPackage/$overlayPackage.apk")
            ShellUtils.setPermissions(644, "$systemApp/$overlayPackage/$overlayPackage.apk")
            remountRO("/system")
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + Utils.getOverlayPackageName(packageName), true)
        } else {
            addAppToUninstall(context, Utils.getOverlayPackageName(packageName))
        }
    }
}