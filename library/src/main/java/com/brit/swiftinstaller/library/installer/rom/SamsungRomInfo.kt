/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.ui.activities.CustomizeActivity
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPath
import java.io.File

class SamsungRomInfo(context: Context) : RomInfo(context) {

    override fun requiresRoot(): Boolean {
        return false
    }

    override fun getChangelogTag(): String {
        return "samsung"
    }

    override fun getDisabledOverlays(): ArrayList<String> {
        val disable = ArrayList<String>()
        disable.add("com.android.emergency")
        return disable
    }

    override fun getRequiredApps(): Array<String> {
        return Array(28) {
            when (it) {
                0 -> "android"
                1 -> "com.android.systemui"
                2 -> "com.amazon.clouddrive.photos"
                3 -> "com.android.settings"
                4 -> "com.anydo"
                5 -> "com.apple.android.music"
                6 -> "com.ebay.mobile"
                7 -> "com.embermitre.pixolor.app"
                8 -> "com.google.android.apps.genie.geniewidget"
                9 -> "com.google.android.apps.inbox"
                10 -> "com.google.android.apps.messaging"
                11 -> "com.google.android.gm"
                12 -> "com.google.android.talk"
                13 -> "com.mxtech.videoplayer.ad"
                14 -> "com.mxtech.videoplayer.pro"
                15 -> "com.pandora.android"
                16 -> "com.simplecity.amp.pro"
                17 -> "com.Slack"
                18 -> "com.samsung.android.incallui"
                19 -> "com.twitter.android"
                20 -> "com.samsung.android.contacts"
                21 -> "com.samsung.android.scloud"
                22 -> "com.samsung.android.themestore"
                23 -> "com.samsung.android.lool"
                24 -> "com.samsung.android.samsungpassautofill"
                25 -> "com.google.android.gms"
                26 -> "com.sec.android.daemonapp"
                27 -> "de.axelspringer.yana.zeropage"
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