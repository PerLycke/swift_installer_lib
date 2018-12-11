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
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName

open class OreoRomHandler(context: Context) : RomHandler(context) {

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            if (targetPackage == "android") {
                if (disableOverlayCommand(targetPackage)) {
                    runCommand("pm install -r $overlayPath", true)
                }
            } else {
                runCommand("pm install -r $overlayPath", true)
            }
            if (runCommand("cmd overlay list", true).output?.contains(getOverlayPackageName(targetPackage)) == true) {
                context.swift.prefs.edit().putBoolean("noSecondReboot", true).apply()
            }
        }
    }

    override fun getChangelogTag(): String {
        return "oreo"
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
                "com.lastpass.lpandroid",
                "com.weather.Weather",
                "com.google.android.inputmethod.latin"
        )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("4285f4")
    }

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {

        if (!uninstall && oppositeApps.isNotEmpty()) {
            oppositeApps.forEach { app ->
                uninstallOverlay(context, app)
            }
        }

        if (intent != null) {
            context.applicationContext.startActivity(intent)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("cmd overlay disable ${getOverlayPackageName(packageName)}", true)
            runCommand("pm uninstall " + getOverlayPackageName(packageName), true)
        }
    }

    override fun disableOverlay(targetPackage: String) {
        runCommand("cmd overlay disable ${getOverlayPackageName(targetPackage)}", true)
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {

            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["sender_name_fix"] = "default"
                selection["notif_background"] = "white"
                selection["qs_alpha"] = "0"
                return selection
            }

            override fun createPreviewHandler(context: Context): PreviewHandler {
                return OreoPreviewHandler(context)
            }
            override fun populateCustomizeOptions(categories: CategoryMap) {
                populateOreoCustomizeOptions(categories)
                super.populateCustomizeOptions(categories)
            }
        }
    }

    class OreoPreviewHandler(context: Context) : PreviewHandler(context) {
        override fun updateIcons(selection: CustomizeSelection) {
            super.updateIcons(selection)
            settingsIcons.forEach { icon ->
                icon.setColorFilter(selection.accentColor)
                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_aosp"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
            }
            systemUiIcons.forEach { icon ->
                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_aosp"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
                icon.setColorFilter(selection.accentColor)
            }
        }
    }
    private fun populateOreoCustomizeOptions(categories: CategoryMap) {
        val notifBackgroundOptions = OptionsMap()
        notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
        notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
        notifBackgroundOptions["dark"]!!.infoText =
                context.getString(R.string.notif_fix_desc_summary)
        val senderNameOptions = OptionsMap()
        senderNameOptions.add(Option(context.getString(R.string.disable), "default"))
        senderNameOptions.add(Option(context.getString(R.string.enable_shadow_title), "shadow"))
        notifBackgroundOptions["dark"]!!.subOptions.putAll(senderNameOptions)
        notifBackgroundOptions["dark"]!!.subOptionKey = "sender_name_fix"
        categories.add(CustomizeCategory(context.getString(R.string.notification_tweaks),
                "notif_background", "white", notifBackgroundOptions, synchronizedArrayListOf("android")))

        val qsOptions = OptionsMap()
        val trans =
                SliderOption(context.getString(R.string.qs_transparency), "qs_alpha")
        trans.current = 0
        qsOptions.add(trans)
        categories.add(CustomizeCategory(context.getString(R.string.quick_settings_style),
                "qs_alpha", "0", qsOptions,
                synchronizedArrayListOf("android")))
    }
}