package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import com.brit.swiftinstaller.library.ui.customize.CategoryMap
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.runCommand
import com.brit.swiftinstaller.library.utils.swift

class OOSPRomHandler(context: Context) : PRomHandler(context) {

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {
        super.postInstall(uninstall, apps, oppositeApps, intent)
        if (apps.contains("android")) {
            runCommand("settings put system oem_black_mode_accent_color \'#${Integer.toHexString(
                    context.swift.selection.accentColor)}\'", true)

        }
    }

    override fun getChangelogTag(): String {
        return "oos-p"
    }

    override fun getRequiredApps(): Array<String> {
        return arrayOf(
                "android",
                "com.android.systemui",
                "com.amazon.clouddrive.photos",
                "com.android.settings",
                "com.anydo",
                "com.apple.android.music",
                "com.ebay.mobile",
                "com.embermitre.pixolor.app",
                "com.google.android.apps.genie.geniewidget",
                "com.google.android.apps.inbox",
                "com.google.android.gm",
                "com.google.android.talk",
                "com.mxtech.videoplayer.ad",
                "com.mxtech.videoplayer.pro",
                "com.pandora.android",
                "com.simplecity.amp.pro",
                "com.Slack",
                "com.twitter.android",
                "com.google.android.gms",
                "com.google.android.apps.nexuslauncher",
                "com.oneplus.deskclock",
                "com.lastpass.lpandroid",
                "com.weather.Weather",
                "com.google.android.settings.intelligence",
                "com.google.android.inputmethod.latin"
        )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("42a5f5")
    }


    override fun populatePieCustomizeOptions(categories: CategoryMap) {
    }
}