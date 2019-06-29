@file:Suppress("MemberVisibilityCanBePrivate")

package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.util.Log
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.RebootActivity
import com.topjohnwu.superuser.io.SuFile

object MagiskUtils {

    const val MAGISK_SYSTEM_PATH = "/sbin/.magisk/mirror/system"
    const val MAGISK_PATH = "/sbin/.magisk/img"
    const val MAGISK_MODULE_PATH = "$MAGISK_PATH/swift_installer"

    const val MIN_MAGISK_VERSION = 17000

    fun convertToMagisk(context: Context) {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        remountRW(MAGISK_SYSTEM_PATH)
        if (getMagiskVersion() < MIN_MAGISK_VERSION) {
            Log.d("TEST", "magisk version not right")
            // TODO: show dialog or notification here.
            return
        }
        createModuleIfNeeded(context)
        var shouldReboot = false
        overlays.forEach { packageName ->
            val opn = OverlayUtils.getOverlayPackageName(packageName)
            val systemFile = SuFile("$MAGISK_SYSTEM_PATH/app/$opn/$opn.apk")
            val magiskFile = SuFile("$MAGISK_MODULE_PATH/system/app/$opn/$opn.apk")
            Log.d("TEST", "systemFile - $systemFile")
            Log.d("TEST", "magiskFile - $magiskFile")
            if (systemFile.exists()) {
                val appPath = if (context.pm.isAppInstalled(opn)) {
                    context.pm.getAppPath(opn)
                } else {
                    systemFile.absolutePath
                }
                Log.d("TEST", "appPath - $appPath")
                if (magiskFile.exists()) {
                    Log.d("TEST", "magisk file already exists")
                    val soi = context.pm.getPackageArchiveInfo(appPath, 0)
                    val moi = context.pm.getPackageArchiveInfo(magiskFile.absolutePath, 0)
                    if (soi.getVersionCode() <= moi.getVersionCode()) {
                        Log.d("TEST", "magisk file is greater then or equal to system file")
                        return@forEach
                    }
                }
                shouldReboot = true
                Log.d("TEST", "create folders and copy files")
                var success = ShellUtils.mkdir(magiskFile.parent)
                Log.d("TEST", "mkdir success - $success")
                success = ShellUtils.copyFile(appPath, magiskFile.absolutePath)
                Log.d("TEST", "copy success 0 $success")
                ShellUtils.setPermissions(755, magiskFile.absolutePath)
                success = deleteFileRoot(systemFile.parent)
                Log.d("TEST", "deleted file - $success")
            }
        }
        remountRO(MAGISK_SYSTEM_PATH)
        if (shouldReboot) {
            RebootActivity.launchRebootActivity(context,
                    context.getString(R.string.reboot_dialog_magisk_msg), true)
        }
    }

    fun convertFromMagisk(context: Context) {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        remountRW(MAGISK_SYSTEM_PATH)
        if (getMagiskVersion() < MIN_MAGISK_VERSION) {
            // TODO: show dialog or notification
            return
        }
        if (!SuFile(MAGISK_MODULE_PATH).exists()) {
            return
        }
        var shouldReboot = false
        overlays.forEach { packageName ->
            val opn = OverlayUtils.getOverlayPackageName(packageName)
            val systemFile = SuFile("$MAGISK_SYSTEM_PATH/app/$opn/$opn.apk")
            val magiskFile = SuFile("$MAGISK_MODULE_PATH/system/app/$opn/$opn.apk")
            if (magiskFile.exists()) {
                shouldReboot = true
                if (systemFile.exists()) {
                    val soi = context.pm.getPackageArchiveInfo(systemFile.absolutePath, 0)
                    val moi = context.pm.getPackageArchiveInfo(magiskFile.absolutePath, 0)
                    if (moi.getVersionCode() <= soi.getVersionCode()) {
                        return@forEach
                    }
                }
                ShellUtils.mkdir(systemFile.absolutePath)
                ShellUtils.copyFile(magiskFile.absolutePath, systemFile.absolutePath)
                ShellUtils.setPermissions(755, systemFile.absolutePath)
                deleteFileRoot(magiskFile.parent)
            }
        }
        deleteFileRoot(MAGISK_MODULE_PATH)
        remountRO(MAGISK_SYSTEM_PATH)
        if (shouldReboot) {
            RebootActivity.launchRebootActivity(context,
                    context.getString(R.string.reboot_dialog_magisk_msg), true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun createModuleIfNeeded(context: Context) {
        //if (context.disableMagisk) return
        //if (!SuFile(MAGISK_MODULE_PATH).exists() && SuFile(MAGISK_PATH).exists()) {
            runCommand("mkdir -p $MAGISK_MODULE_PATH", true)
            runCommand("chmod -R 755 $MAGISK_MODULE_PATH", true)
            runCommand("touch $MAGISK_MODULE_PATH/auto_mount", true)
            runCommand("touch $MAGISK_MODULE_PATH/update", true)
            var moduleProp = "id=swift_installer\n"
            moduleProp += "name=Swift Installer Module\n"
            moduleProp += "version=1.2\n"
            moduleProp += "versionCode=3\n"
            moduleProp += "author=gmillz\n"
            moduleProp += "description=Generated By Swift Installer\n"
            runCommand("echo \"$moduleProp\" > $MAGISK_MODULE_PATH/module.prop", true)
            //RebootActivity.launchRebootActivity(context,
              //      context.getString(R.string.reboot_dialog_magisk_msg), false)
        //}
    }

    private fun getMagiskVersion(): Int {
        val ver = (runCommand("magisk -V").output?: "0").trim()
        return ver.toInt()
    }

    val magiskEnabled = !SuFile(MAGISK_MODULE_PATH, "disable").exists()
            && SuFile(MAGISK_PATH).exists()
}
