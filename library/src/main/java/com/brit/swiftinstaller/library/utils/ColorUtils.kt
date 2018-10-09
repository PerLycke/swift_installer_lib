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

package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat

object ColorUtils {

    fun toHexString(color: Int): String {
        return Integer.toHexString(removeAlpha(color))
    }

    fun getAlphaDimen(i: Int): Float {
        return (100 - i.toFloat()) / 100
    }

    fun checkBackgroundColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

        return darkness > 0.4
    }

    fun checkAccentColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness > 0.2 && darkness < 0.9
    }

    fun getAlpha(color: Int, i: Int): String {
        val a = 255 - (255 * (i.toFloat() / 100))
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = String.format("%02X", (0xFF and Math.round(a)))
        val rgb = String.format("%06X", (0xFFFFFF and Color.rgb(r, g, b)))
        return "$alpha$rgb"
    }

    fun addAlphaColor(color: Int, i: Int): Int {
        val a = 255 - (255 * (i.toFloat() / 100))
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(Math.round(a), r, g, b)
    }

    private fun removeAlpha(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.rgb(r, g, b)
    }

    fun handleColor(color: Int, offset: Int): Int {
        var r = Math.max(Color.red(color), 1)
        var g = Math.max(Color.green(color), 1)
        var b = Math.max(Color.blue(color), 1)

        r += offset
        g += offset
        b += offset

        return Color.rgb(Math.min(Math.max(r, 1), 255), Math.min(Math.max(g, 1), 255),
                Math.min(Math.max(b, 1), 255))
    }

    fun convertToColorInt(color: String): Int {
        var argb = color

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "")
        }

        if (argb.length == 3) {
            var rrggbb = ""
            for (i in 0 until argb.length) {
                rrggbb += argb[i] + "" + argb[i]
            }
            argb = rrggbb
        }

        var alpha = -1
        var red = -1
        var green = -1
        var blue = -1

        if (argb.length == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16)
            red = Integer.parseInt(argb.substring(2, 4), 16)
            green = Integer.parseInt(argb.substring(4, 6), 16)
            blue = Integer.parseInt(argb.substring(6, 8), 16)
        } else if (argb.length == 6) {
            alpha = 255
            red = Integer.parseInt(argb.substring(0, 2), 16)
            green = Integer.parseInt(argb.substring(2, 4), 16)
            blue = Integer.parseInt(argb.substring(4, 6), 16)
        }

        return Color.argb(alpha, red, green, blue)
    }

    fun radioButtonColor(context: Context, disabled: Int, accent: Int): ColorStateList {

        return ColorStateList(
                arrayOf(
                        intArrayOf(-android.R.attr.state_checked), //disabled
                        intArrayOf(android.R.attr.state_checked) //enabled
                       ),
                intArrayOf(
                        ContextCompat.getColor(context, disabled)
                        , accent
                          )
                             )
    }

    fun rgbToRgba(rgb: Int): String {
        return rgbToRgba(Integer.toHexString(rgb))
    }

    fun rgbToRgba(rgb: String): String {
        return "$rgb${rgb.substring(0, 2)}".removeRange(0, 2)
    }
}
