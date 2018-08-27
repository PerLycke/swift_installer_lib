package com.brit.swiftinstaller.installer.rom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import android.util.Log
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.ui.activities.CustomizeActivity
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.utils.OverlayUtils.getOverlayPath
import java.io.File

class SamsungRomInfo(context: Context) : RomInfo(context) {

    override fun getDisabledOverlays(): ArrayList<String> {
        val disable = ArrayList<String>()
        disable.add("com.android.emergency")
        return disable
    }

    override fun getRequiredApps(): Array<String> {
        return Array(29) {
            when (it) {
                0 -> "android"
                1 -> "com.android.systemui"
                2 -> "com.amazon.clouddrive.photos"
                3 -> "com.android.settings"
                4 -> "com.android.systemui"
                5 -> "com.anydo"
                6 -> "com.apple.android.music"
                7 -> "com.ebay.mobile"
                8 -> "com.embermitre.pixolor.app"
                9 -> "com.google.android.apps.genie.geniewidget"
                10 -> "com.google.android.apps.inbox"
                11 -> "com.google.android.apps.messaging"
                12 -> "com.google.android.gm"
                13 -> "com.google.android.talk"
                14 -> "com.mxtech.videoplayer.ad"
                15 -> "com.mxtech.videoplayer.pro"
                16 -> "com.pandora.android"
                17 -> "com.simplecity.amp.pro"
                18 -> "com.Slack"
                19 -> "com.samsung.android.incallui"
                20 -> "com.twitter.android"
                21 -> "com.samsung.android.contacts"
                22 -> "com.samsung.android.scloud"
                23 -> "com.samsung.android.themestore"
                24 -> "com.samsung.android.lool"
                25 -> "com.samsung.android.samsungpassautofill"
                26 -> "com.google.android.gms"
                27 -> "com.sec.android.daemonapp"
                28 -> "de.axelspringer.yana.zeropage"
                else -> ""
            }
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
        val extraIntent = intent != null

        if (ShellUtils.isRootAvailable) {
            if (!uninstall && oppositeApps != null && oppositeApps.isNotEmpty()) {
                for (app in oppositeApps) {
                    uninstallOverlay(context, app)
                }
            }
            if (intent != null) {
                context.applicationContext.startActivity(intent)
            }
            return
        }

        if (apps.contains("android")) {
            val index = apps.indexOf("android")
            apps.removeAt(index)
            apps.add(0, "android")
        }
        if (apps.contains("com.google.android.packageinstaller")) {
            val index = apps.indexOf("com.google.android.packageinstaller")
            apps.removeAt(index)
            apps.add(0, "com.google.android.packageinstaller")
        }

        val intents = Array(if (!extraIntent) {
            apps.size
        } else {
            apps.size + 1
        }) { i ->
            val index = if (extraIntent) {
                i - 1
            } else {
                i
            }
            if (!extraIntent || i > 0) {
                val appInstall = Intent()
                if (uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(apps.elementAt(index)), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            "com.brit.swiftinstaller.myprovider",
                            File(getOverlayPath(apps.elementAt(index))))
                    Log.d("TEST", "file exists ? ${File(getOverlayPath(apps.elementAt(index))).exists()}")
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            } else {
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
        }

        if (!intents.isEmpty()) {
            context.startActivities(intents)
        }

        if (oppositeApps != null && !oppositeApps.isEmpty()) {
            val oppositeIntents = Array(oppositeApps.size) {
                val appInstall = Intent()
                if (!uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(oppositeApps[it]), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".myprovider",
                            File(getOverlayPath(oppositeApps[it])))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            }
            context.startActivities(oppositeIntents)
        }

        clearAppsToUninstall(context)
        clearAppsToInstall(context)
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        Log.d("TEST", "installOverlay")
        Log.d("TEST", "path - $overlayPath")
        Log.d("TEST", "exists - ${File(overlayPath).exists()}")
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + getOverlayPackageName(packageName), true)
        } else {
            addAppToUninstall(context, getOverlayPackageName(packageName))
        }
    }

    override fun getCustomizeFeatures(): Int {
        return if (Build.VERSION.SDK_INT == 26) {
            super.getCustomizeFeatures()
        } else {
            CustomizeActivity.SUPPORTS_CLOCK + CustomizeActivity.SUPPORTS_ICONS + CustomizeActivity.SUPPORTS_TRANSPARENCY +
                    CustomizeActivity.SUPPORTS_SHADOW
        }
    }
}