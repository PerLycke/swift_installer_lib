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

package com.brit.swiftinstaller.library.ui.customize

import android.content.Context
import android.preference.PreferenceManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ColorUtils.convertToColorInt

abstract class CustomizeHandler(val context: Context) {

    private var previewHandler: PreviewHandler? = null

    class PaletteItem {
        constructor(backgroundColor: Int, backgroundName: String) {
            this.backgroundColor = backgroundColor
            this.backgroundName = backgroundName
        }

        constructor(accentColor: Int) {
            this.accentColor = accentColor
        }
        var backgroundColor = -1
        var backgroundName = ""
        var accentColor = -1
    }

    open fun getAccentColors(): ArrayList<PaletteItem> {
        val accents = arrayListOf<PaletteItem>()
        accents.add(PaletteItem(context.getColor(R.color.minimal_green)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_blue)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_orange)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_red)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_tangerine)))
        accents.add(PaletteItem(context.getColor(R.color.blue)))
        accents.add(PaletteItem(context.getColor(R.color.red)))
        accents.add(PaletteItem(context.getColor(R.color.green)))
        accents.add(PaletteItem(context.getColor(R.color.amber)))
        accents.add(PaletteItem(context.getColor(R.color.violet)))
        return accents
    }

    open fun getBackgroundColors(): ArrayList<PaletteItem> {
        val backgrounds = arrayListOf<PaletteItem>()
        backgrounds.add(PaletteItem(convertToColorInt("202026"), context.getString(R.string.swift_dark)))
        backgrounds.add(PaletteItem(convertToColorInt("000000"), context.getString(R.string.swift_black)))
        backgrounds.add(PaletteItem(convertToColorInt("202833"), context.getString(R.string.swift_style)))
        backgrounds.add(PaletteItem(convertToColorInt("1C3B3A"), context.getString(R.string.bg_nature)))
        backgrounds.add(PaletteItem(convertToColorInt("173145"), context.getString(R.string.bg_ocean)))
        backgrounds.add(PaletteItem(convertToColorInt("363844"), context.getString(R.string.night)))
        return backgrounds
    }

    fun getPreviewHandler(): PreviewHandler {
        if (previewHandler == null) {
            previewHandler = createPreviewHandler()
        }
        return previewHandler!!
    }

    open fun createPreviewHandler() : PreviewHandler {
        return object : PreviewHandler(context) {

        }
    }

    fun getSelection(): CustomizeSelection {
        return CustomizeSelection.fromString(PreferenceManager
                .getDefaultSharedPreferences(context).getString("customize_options", getDefaultSelection().toString())!!)
    }

    fun setSelection(selection: CustomizeSelection) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("customize_selection", selection.toString()).apply()
    }

    open fun getDefaultSelection(): CustomizeSelection {
        val selection = CustomizeSelection()
        for (option in getCustomizeOptions()) {
            selection[option.key] = option.default
            for (opt in option.options) {
                if (opt.subOptions.isNotEmpty()) {
                    selection[opt.subOptionKey] = opt.subOptionDefault
                }
                if (opt.isSliderOption) {
                    selection[opt.value] = (opt as SliderOption).current.toString()
                }
            }
        }
        return selection
    }

    open fun getCustomizeOptions() : CategoryMap {
        val requiredApps = ArrayList<String>()
        val categories = CategoryMap()
        val iconOptions = OptionsMap()
        iconOptions.add(Option(context.getString(R.string.aosp_icons), "aosp", "aosp", true))
        iconOptions.add(Option(context.getString(R.string.stock_icons), "stock", "stock", true))
        iconOptions.add(Option(context.getString(R.string.stock_icons_multi), "stock_multi", "stock", false))
        iconOptions.add(Option(context.getString(R.string.android_p), "p", "p", false))
        requiredApps.add("com.android.systemui")
        requiredApps.add("com.samsung.android.lool")
        requiredApps.add("com.samsung.android.themestore")
        requiredApps.add("com.android.settings")
        requiredApps.add("com.samsung.android.app.aodservice")
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.category_icons), "icons", "stock", iconOptions, requiredApps))
        requiredApps.clear()

        val clockOptions = OptionsMap()
        clockOptions.add(Option(context.getString(R.string.right), "right"))
        clockOptions.add(Option(context.getString(R.string.left), "left"))
        clockOptions.add(Option(context.getString(R.string.centered), "centered"))
        requiredApps.add("com.android.systemui")
        categories.add(CustomizeCategory(context.getString(R.string.clock), "clock", "right", clockOptions, requiredApps))
        requiredApps.clear()

        val notifOptions = OptionsMap()
        notifOptions.add(Option(context.getString(R.string.default_style), "default"))
        notifOptions.add(Option(context.getString(R.string.android_p_rounded_style), "p"))
        val trans = SliderOption(context.getString(R.string.qs_transparency), "qsAlpha")
        trans.current = 0
        notifOptions.add(trans)
        notifOptions["p"]!!.infoDialogTitle = context.getString(R.string.rounded_dialog_title)
        notifOptions["p"]!!.infoDialogText = context.getString(R.string.rounded_dialog_info)
        requiredApps.add("com.android.systemui")
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.notification_style), "style", "default", notifOptions, requiredApps))
        requiredApps.clear()

        val notifBackgroundOptions = OptionsMap()
        notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
        notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
        notifBackgroundOptions["dark"]!!.infoText = context.getString(R.string.notif_fix_desc_summary)
        val senderNameOptions = OptionsMap()
        senderNameOptions.add(Option(context.getString(R.string.disable), "default"))
        senderNameOptions.add(Option(context.getString(R.string.enable_shadow_title), "shadow"))
        notifBackgroundOptions["dark"]!!.subOptions.putAll(senderNameOptions)
        notifBackgroundOptions["dark"]!!.subOptionKey = "shadow_fix"
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.notification_tweaks), "notif_background", "white", notifBackgroundOptions, requiredApps))
        requiredApps.clear()

        return categories
    }
}