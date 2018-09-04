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
import android.util.Log

@Suppress("unused")
object ColorUtils {

    private const val TAG = "ColorUtils"

    fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

        return darkness > 0.5
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

    fun removeAlpha(color: Int): Int {
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

        return Color.rgb(Math.min(Math.max(r, 1), 255), Math.min(Math.max(g, 1), 255), Math.min(Math.max(b, 1), 255))
    }

    fun changeColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        return Color.argb(a,
                Math.max((r * factor).toInt(), 0),
                Math.max((g * factor).toInt(), 0),
                Math.max((b * factor).toInt(), 0) - 1)
    }

    fun changeColor(color: Int, rf: Float, bf: Float, gf: Float): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        return Color.argb(a,
                Math.max((r * rf).toInt(), 0),
                Math.max((g * bf).toInt(), 0),
                Math.max((b * gf).toInt(), 0))
    }

    fun lightenColor(color: Int): Int {
        val factor = 1.2f
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        return Color.argb(a,
                Math.max((r * factor).toInt(), 0),
                Math.max((g * factor).toInt(), 0),
                Math.max((b * factor).toInt(), 0))
    }

    fun getFactor(originalColor: Int, newColor: Int) {
        val or = Math.max(Color.red(originalColor), 1)
        val og = Math.max(Color.green(originalColor), 1)
        val ob = Math.max(Color.blue(originalColor), 1)

        val nr = Color.red(newColor)
        val ng = Color.green(newColor)
        val nb = Color.blue(newColor)

        Log.d(TAG, "red factor ${(nr.toDouble() / or)}")
        Log.d(TAG, "green factor ${ng.toDouble() / og}")
        Log.d(TAG, "blue factor ${nb.toDouble() / ob}")
    }

    fun printRGB(color: Int) {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        Log.d(TAG, "r: $r | g: $g | b: $b")
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

    fun radioButtonColor(context: Context, disabled: Int, accent: Int) : ColorStateList {

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
}
