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
import com.brit.swiftinstaller.library.ui.activities.CustomizeActivity
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.ShellUtils
import com.brit.swiftinstaller.library.utils.runCommand

open class OreoRomInfo(context: Context) : RomInfo(context) {

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    override fun getChangelogTag(): String {
        return "oreo"
    }

    override fun getRequiredApps(): Array<String> {
        return Array(23) {
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
                18 -> "com.twitter.android"
                19 -> "com.google.android.gms"
                20 -> "com.google.android.apps.nexuslauncher"
                21 -> "com.lastpass.lpandroid"
                22 -> "com.weather.Weather"
                else -> ""
            }
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
        if (ShellUtils.isRootAvailable && !uninstall) {
            for (app in apps) {
                runCommand("cmd overlay enable " + getOverlayPackageName(app), true)
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
            runCommand("pm uninstall " + getOverlayPackageName(packageName), true)
        }
    }

    override fun getCustomizeFeatures() : Int {
        return CustomizeActivity.SUPPORTS_SHADOW
    }

    override fun disableOverlay(targetPackage: String) {
        runCommand("cmd overlay disable ${getOverlayPackageName(targetPackage)}", true)
    }

    override fun useHotSwap(): Boolean { return true }
}