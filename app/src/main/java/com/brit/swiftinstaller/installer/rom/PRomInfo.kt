package com.brit.swiftinstaller.installer.rom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.utils.*
import java.io.File

class PRomInfo(context: Context, name: String, version: String) : RomInfo(context, name, version) {

    private val SYSTEM_APP = "/system/app"

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        val installed = Utils.isOverlayInstalled(context, Utils.getOverlayPackageName(targetPackage))
        val overlayPackage = Utils.getOverlayPackageName(targetPackage)
        if (ShellUtils.isRootAvailable) {
            remountRW("/system")
            //runCommand("mkdir -p /data/swift/overlays/${Utils.getOverlayPackageName(targetPackage)}")
            ShellUtils.mkdir("$SYSTEM_APP/$overlayPackage")
            ShellUtils.copyFile(overlayPath, "$SYSTEM_APP/$overlayPackage/$overlayPackage.apk")
            ShellUtils.setPermissions(644, "$SYSTEM_APP/$overlayPackage/$overlayPackage.apk")
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