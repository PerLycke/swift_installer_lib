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
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ColorUtils.convertToColorInt
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.prefs
import com.brit.swiftinstaller.library.utils.swift

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
        selection.accentColor = context.swift.romHandler.getDefaultAccent()
        selection.backgroundColor = convertToColorInt("16161c")
        return selection
    }

    open fun populateCustomizeOptions(categories: CategoryMap) {

    }

    fun getCustomizeOptions(): CategoryMap {
        return categories
    }
}