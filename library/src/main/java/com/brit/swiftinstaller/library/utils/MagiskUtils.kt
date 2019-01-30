package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.content.Intent
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.installer.rom.RomHandler
import com.brit.swiftinstaller.library.ui.activities.RebootActivity
import com.topjohnwu.superuser.io.SuFile

object MagiskUtils {
    fun convertToMagisk(context: Context) {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        remountRW("/system")
        if (getMagiskVersion() < 17000) {
            // show dialog or notification here. need activity for dialog as this is run on boot.
            return
        }
        var shouldReboot = false
        overlays.forEach { packageName ->
            val opn = OverlayUtils.getOverlayPackageName(packageName)
            val systemFile = SuFile("/system/app/$opn/$opn.apk")
            val magiskFile = SuFile("${RomHandler.magiskPath}/${systemFile.absolutePath}")
            val overlayInstalled = context.pm.isAppInstalled(opn)
            if (overlayInstalled || systemFile.exists()) {
                shouldReboot = true
                val appPath = if (overlayInstalled) {
                    context.pm.getAppPath(opn)
                } else {
                    systemFile.absolutePath
                }
                if (!magiskFile.exists()) {
                    ShellUtils.mkdir(magiskFile.parent)
                    ShellUtils.copyFile(appPath, magiskFile.absolutePath)
                    ShellUtils.setPermissions(755, magiskFile.absolutePath)
                } else {
                    val soi = context.packageManager.getPackageArchiveInfo(
                            appPath, 0)
                    val moi = context.packageManager.getPackageArchiveInfo(
                            magiskFile.absolutePath, 0)
                    if (soi.getVersionCode() > moi.getVersionCode()) {
                        ShellUtils.copyFile(appPath, magiskFile.absolutePath)
                        ShellUtils.setPermissions(755, magiskFile.absolutePath)
                    }
                }
                if (overlayInstalled) {
                    runCommand("pm uninstall " + OverlayUtils.getOverlayPackageName(packageName), true)
                } else {
                    deleteFileRoot(systemFile.parent)
                }
            }
        }
        remountRO("/system")
        if (shouldReboot) {
            val reboot = Intent(context, RebootActivity::class.java)
            reboot.flags += Intent.FLAG_ACTIVITY_NEW_TASK
            reboot.putExtra("message", context.getString(R.string.reboot_dialog_magisk_msg))
            context.startActivity(reboot)
        }
    }

    fun convertFromMagisk(context: Context) {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        remountRW("/system")
        if (getMagiskVersion() < 17000) {
            // show dialog or notification here. need activity for dialog as this is run on boot.
            return
        }
        if (!SuFile(RomHandler.magiskPath).exists()) {
            return
        }
        overlays.forEach { packageName ->
            val opn = OverlayUtils.getOverlayPackageName(packageName)
            val systemFile = SuFile("/system/app/$opn/$opn.apk")
            val magiskFile = SuFile("${RomHandler.magiskPath}/${systemFile.absolutePath}")
            if (magiskFile.exists()) {
                if (!systemFile.exists()) {
                    ShellUtils.mkdir(systemFile.parent)
                    ShellUtils.copyFile(magiskFile.absolutePath, systemFile.absolutePath)
                    ShellUtils.setPermissions(755, systemFile.absolutePath)
                    deleteFileRoot(magiskFile.parent)
                } else {
                    val soi = context.packageManager.getPackageArchiveInfo(
                            systemFile.absolutePath, 0)
                    val moi = context.packageManager.getPackageArchiveInfo(
                            magiskFile.absolutePath, 0)
                    if (moi.getVersionCode() > soi.getVersionCode()) {
                        ShellUtils.copyFile(magiskFile.absolutePath, systemFile.absolutePath)
                        ShellUtils.setPermissions(755, systemFile.absolutePath)
                        deleteFileRoot(magiskFile.parent)
                    }
                }
            }
        }
        deleteFileRoot(RomHandler.magiskPath)
        remountRO("/system")
        val reboot = Intent(context, RebootActivity::class.java)
        reboot.flags += Intent.FLAG_ACTIVITY_NEW_TASK
        reboot.putExtra("message", context.getString(R.string.reboot_dialog_magisk_msg))
        context.startActivity(reboot)
    }

    fun createModule(context: Context) {
        if (context.disableMagisk) return
        if (!SuFile(RomHandler.magiskPath).exists() && SuFile("/sbin/.core/img").exists()) {
            runCommand("mkdir -p ${RomHandler.magiskPath}")
            runCommand("chmod -R 755 ${RomHandler.magiskPath}")
            runCommand("touch ${RomHandler.magiskPath}/auto_mount", true)
            runCommand("touch ${RomHandler.magiskPath}/update", true)
            var moduleProp = "id=swift_installer\n"
            moduleProp += "name=Swift Installer Module\n"
            moduleProp += "version=v1.2\n"
            moduleProp += "versionCode=3\n"
            moduleProp += "author=gmillz\n"
            moduleProp += "description=Generated By Swift Installer\n"
            runCommand("echo \"$moduleProp\" > "
                    + "${RomHandler.magiskPath}/module.prop", true)
            val reboot = Intent(context, RebootActivity::class.java)
            reboot.flags += Intent.FLAG_ACTIVITY_NEW_TASK
            reboot.putExtra("message", context.getString(R.string.reboot_dialog_magisk_msg))
            context.startActivity(reboot)
        }
    }

    private fun getMagiskVersion(): Int {
        val ver = (runCommand("magisk -V").output?: "0").trim()
        return ver.toInt()
    }
}