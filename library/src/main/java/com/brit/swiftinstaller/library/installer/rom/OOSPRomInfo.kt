package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import com.brit.swiftinstaller.library.utils.getAccentColor
import com.brit.swiftinstaller.library.utils.runCommand

class OOSPRomInfo(context: Context) : PRomInfo(context) {

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>,
                             oppositeApps: ArrayList<String>?, intent: Intent?) {
        super.postInstall(uninstall, apps, oppositeApps, intent)
        if (apps.contains("android")) {
            runCommand("settings put system oem_black_mode_accent_color \'#${Integer.toHexString(getAccentColor(context))}\'", true)

        }
    }

    override fun getChangelogTag(): String {
        return "oos-p"
    }

    override fun getRequiredApps(): Array<String> {
        return Array(25) {
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
                21 -> "com.oneplus.deskclock"
                22 -> "com.lastpass.lpandroid"
                23 -> "com.weather.Weather"
                24 -> "com.google.android.settings.intelligence"
                else -> ""
            }
        }
    }
}