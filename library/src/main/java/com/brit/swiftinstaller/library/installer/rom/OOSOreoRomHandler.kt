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
import kotlinx.android.synthetic.main.customize_preview_settings.view.*

class OOSOreoRomHandler(context: Context) : OreoRomHandler(context) {

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {
        super.postInstall(uninstall, apps, oppositeApps, intent)
        if (apps.contains("android")) {
            runCommand("settings put system oem_black_mode_accent_color \'#${Integer.toHexString(
                    context.swift.selection.accentColor)}\'", true)

        }
    }

    override fun getChangelogTag(): String {
        return "oos-oreo"
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
                "com.google.android.inputmethod.latin")
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("42a5f5")
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {

            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["oos_oreo_clock"] = "oos_right"
                selection["sender_name_fix"] = "default"
                selection["notif_background"] = "white"
                selection["qs_alpha"] = "0"
                return selection
            }

            override fun populateCustomizeOptions(categories: CategoryMap) {
                populateOreoCustomizeOptions(categories)
                super.populateCustomizeOptions(categories)
            }

            override fun createPreviewHandler(context: Context): PreviewHandler {
                return OreoPreviewHandler(context)
            }
        }
    }

    class OreoPreviewHandler(context: Context) : PreviewHandler(context) {
        override fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
            super.updateView(palette, selection)
            settingsPreview?.let {
                if (selection.containsKey("oos_oreo_clock")) {
                    val clockSelection = selection["oos_oreo_clock"]
                    it.clock_right.setVisible(clockSelection == "oos_right")
                    it.clock_left.setVisible(clockSelection == "oos_left")
                    it.clock_centered.setVisible(clockSelection == "oos_centered")
                }
            }
        }

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
        val clockOptions = OptionsMap()
        clockOptions.add(Option(context.getString(R.string.right), "oos_right"))
        clockOptions.add(Option(context.getString(R.string.left), "oos_left"))
        clockOptions.add(Option(context.getString(R.string.centered), "oos_centered"))
        categories.add(
                CustomizeCategory(context.getString(R.string.clock), "oos_oreo_clock", "oos_right",
                        clockOptions, synchronizedArrayListOf("com.android.systemui")))
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