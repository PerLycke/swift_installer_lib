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
import android.os.Build
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ColorUtils.convertToColorInt
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.prefs
import com.brit.swiftinstaller.library.utils.swift
import com.brit.swiftinstaller.library.utils.synchronizedArrayListOf

abstract class CustomizeHandler(val context: Context) {

    private val accents = SynchronizedArrayList<PaletteItem>()
    private val backgrounds = SynchronizedArrayList<PaletteItem>()
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

    open fun populateAccentColors(accents: SynchronizedArrayList<PaletteItem>) {
        accents.add(PaletteItem(context.swift.romHandler.getDefaultAccent()))
        accents.add(PaletteItem(context.getColor(R.color.minimal_orange)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_red)))
        accents.add(PaletteItem(context.getColor(R.color.green)))
        accents.add(PaletteItem(context.getColor(R.color.blue)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_tangerine)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_green)))
        accents.add(PaletteItem(context.getColor(R.color.minimal_blue)))
        accents.add(PaletteItem(context.getColor(R.color.violet)))
        accents.add(PaletteItem(context.getColor(R.color.red)))
    }

    fun getAccentColors(): SynchronizedArrayList<PaletteItem> {
        return accents
    }

    open fun populateBackgroundColors(backgrounds: SynchronizedArrayList<PaletteItem>) {
        backgrounds.add(
                PaletteItem(convertToColorInt("202026"), context.getString(R.string.swift_dark)))
        backgrounds.add(
                PaletteItem(convertToColorInt("000000"), context.getString(R.string.swift_black)))
        backgrounds.add(
                PaletteItem(convertToColorInt("202833"), context.getString(R.string.swift_style)))
        backgrounds.add(
                PaletteItem(convertToColorInt("1C3B3A"), context.getString(R.string.bg_nature)))
        backgrounds.add(
                PaletteItem(convertToColorInt("173145"), context.getString(R.string.bg_ocean)))
        backgrounds.add(PaletteItem(convertToColorInt("363844"), context.getString(R.string.night)))
    }

    fun getBackgroundColors(): SynchronizedArrayList<PaletteItem> {
        return backgrounds
    }

    open fun createPreviewHandler(context: Context): PreviewHandler {
        return object : PreviewHandler(context) {
        }
    }

    fun getSelection(): CustomizeSelection {
        val selection = CustomizeSelection.fromString(context.prefs
                .getString("customize_selection", getDefaultSelection().toString())!!)
        val def = getDefaultSelection()
        for (key in def.keys) {
            if (!selection.containsKey(key)) {
                selection[key] = def[key]
            }
        }
        return selection
    }

    fun setSelection(selection: CustomizeSelection) {
        context.prefs.edit()
                .putString("customize_selection", selection.toString()).apply()
    }

    open fun getDefaultSelection(): CustomizeSelection {
        val selection = CustomizeSelection()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            selection["sender_name_fix"] = "default"
        }
        selection["notif_background"] = "white"
        selection.accentColor = context.swift.romHandler.getDefaultAccent()
        selection.backgroundColor = convertToColorInt("16161c")
        selection["qs_alpha"] = "0"
        selection["tiles_options"] = "accent_colors"
        return selection
    }

    open fun populateCustomizeOptions(categories: CategoryMap) {

        val notifBackgroundOptions = OptionsMap()
        notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
        notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            notifBackgroundOptions["dark"]!!.infoText =
                    context.getString(R.string.notif_fix_desc_summary)
            val senderNameOptions = OptionsMap()
            senderNameOptions.add(Option(context.getString(R.string.disable), "default"))
            senderNameOptions.add(Option(context.getString(R.string.enable_shadow_title), "shadow"))
            notifBackgroundOptions["dark"]!!.subOptions.putAll(senderNameOptions)
            notifBackgroundOptions["dark"]!!.subOptionKey = "sender_name_fix"
        }
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
        val tilesOptions = OptionsMap()
        tilesOptions.add(Option((context.getString(R.string.tile_option_material_color)), "material_colors", "material", false))
        tilesOptions.add(Option((context.getString(R.string.tile_option_pastel_color)), "pastel_colors", "pastel", false))
        tilesOptions.add(Option((context.getString(R.string.tile_option_accent_color)), "accent_colors", "pastel", true))
        categories.add(
                CustomizeCategory((context.getString(R.string.tile_options)), "tiles_options",
                        "accent_colors", tilesOptions,
                        synchronizedArrayListOf("com.google.android.dialer",
                                "com.google.android.contacts",
                                "com.google.android.apps.messaging",
                                "com.google.android.apps.tachyon"
                        )))
    }

    fun getCustomizeOptions(): CategoryMap {
        return categories
    }
}