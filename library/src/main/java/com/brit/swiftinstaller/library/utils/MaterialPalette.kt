/*
 *
 *  * Copyright (C) 2019 Griffin Millender
 *  * Copyright (C) 2019 Per Lycke
 *  * Copyright (C) 2019 Davide Lilli & Nishith Khanna
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

package com.brit.swiftinstaller.library.utils

import android.content.Context
import com.brit.swiftinstaller.library.utils.ColorUtils.handleColor

class MaterialPalette {
    var backgroundColor = 0
    var cardBackground = 0
    var floatingBackground = 0
    var darkBackgroundColor = 0
    var darkerBackgroundColor = 0
    var lighterBackgroundColor = 0
    var buttonBackground = 0
    var otherBackground = 0

    companion object {
        fun get(context: Context): MaterialPalette {
            return createPalette(context.swift.selection.backgroundColor,
                    useBackgroundPalette(context))
        }

        fun createPalette(color: Int, palette: Boolean): MaterialPalette {
            val p = MaterialPalette()
            if (palette) {
                p.backgroundColor = color
                p.cardBackground = handleColor(color, 8)
                p.floatingBackground = handleColor(color, 3)
                p.darkBackgroundColor = handleColor(color, -5)
                p.darkerBackgroundColor = handleColor(color, -10)
                p.lighterBackgroundColor = handleColor(color, 20)
                p.buttonBackground = handleColor(color, 16)
                p.otherBackground = handleColor(color, 23)
            } else {
                p.backgroundColor = color
                p.cardBackground = color
                p.floatingBackground = color
                p.darkBackgroundColor = handleColor(color, 0)
                p.darkerBackgroundColor = color
                p.lighterBackgroundColor = color
                p.buttonBackground = handleColor(color, 20)
                p.otherBackground = handleColor(color, 33)
            }
            return p
        }
    }

    override fun toString(): String {
        return "backgroundColor - ${Integer.toHexString(backgroundColor)}\n " +
                "cardBackground - ${Integer.toHexString(cardBackground)}\n" +
                "floatingBackground - ${Integer.toHexString(floatingBackground)}\n" +
                "darkBackground - ${Integer.toHexString(darkBackgroundColor)}\n" +
                "darkerBackground - ${Integer.toHexString(darkerBackgroundColor)}\n" +
                "lighterBackground - ${Integer.toHexString(lighterBackgroundColor)}\n" +
                "buttonBackgruond - ${Integer.toHexString(buttonBackground)}\n" +
                "otherBackground - ${Integer.toHexString(otherBackground)}"
    }
}