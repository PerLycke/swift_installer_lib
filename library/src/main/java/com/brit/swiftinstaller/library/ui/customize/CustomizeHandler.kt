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
import com.brit.swiftinstaller.library.utils.swift

abstract class CustomizeHandler(val context: Context) {

    private val accents = arrayListOf<PaletteItem>()
    private val backgrounds = arrayListOf<PaletteItem>()
    private val categories = CategoryMap()
    init {
        initialize()
    }

    private fun initialize() {
        populateCustomizeOptions(categories)
        populateAccentColors(accents)
        populateBackgroundColors(backgrounds)
    }

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

    open fun populateAccentColors(accents: ArrayList<PaletteItem>) {
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
    }

    fun getAccentColors(): ArrayList<PaletteItem> {
        return accents
    }

    open fun populateBackgroundColors(backgrounds: ArrayList<PaletteItem>) {
        backgrounds.add(PaletteItem(convertToColorInt("202026"), context.getString(R.string.swift_dark)))
        backgrounds.add(PaletteItem(convertToColorInt("000000"), context.getString(R.string.swift_black)))
        backgrounds.add(PaletteItem(convertToColorInt("202833"), context.getString(R.string.swift_style)))
        backgrounds.add(PaletteItem(convertToColorInt("1C3B3A"), context.getString(R.string.bg_nature)))
        backgrounds.add(PaletteItem(convertToColorInt("173145"), context.getString(R.string.bg_ocean)))
        backgrounds.add(PaletteItem(convertToColorInt("363844"), context.getString(R.string.night)))
    }

    fun getBackgroundColors(): ArrayList<PaletteItem> {
        return backgrounds
    }

    open fun createPreviewHandler(context: Context) : PreviewHandler {
        return object : PreviewHandler(context) {
        }
    }

    fun getSelection(): CustomizeSelection {
        val selection = CustomizeSelection.fromString(PreferenceManager
                .getDefaultSharedPreferences(context).getString("customize_selection", getDefaultSelection().toString())!!)
        val def = getDefaultSelection()
        for (key in def.keys) {
            if (!selection.containsKey(key)) {
                selection[key] = def[key]
            }
        }
        return selection
    }

    fun setSelection(selection: CustomizeSelection) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("customize_selection", selection.toString()).apply()
    }

    open fun getDefaultSelection(): CustomizeSelection {
        val selection = CustomizeSelection()
        selection["sender_name_fix"] = "default"
        selection["notif_background"] = "white"
        selection.accentColor = context.swift.romInfo.getDefaultAccent()
        selection.backgroundColor = convertToColorInt("16161c")
        return selection
    }

    open fun populateCustomizeOptions(categories: CategoryMap) {
        val requiredApps = ArrayList<String>()

        val notifBackgroundOptions = OptionsMap()
        notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
        notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
        notifBackgroundOptions["dark"]!!.infoText = context.getString(R.string.notif_fix_desc_summary)
        val senderNameOptions = OptionsMap()
        senderNameOptions.add(Option(context.getString(R.string.disable), "default"))
        senderNameOptions.add(Option(context.getString(R.string.enable_shadow_title), "shadow"))
        notifBackgroundOptions["dark"]!!.subOptions.putAll(senderNameOptions)
        notifBackgroundOptions["dark"]!!.subOptionKey = "sender_name_fix"
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.notification_tweaks), "notif_background", "white", notifBackgroundOptions, requiredApps))
        requiredApps.clear()
    }

    fun getCustomizeOptions() : CategoryMap {
        return categories
    }
}