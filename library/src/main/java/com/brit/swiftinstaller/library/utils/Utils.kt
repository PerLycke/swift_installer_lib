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
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.brit.swiftinstaller.library.R

object Utils {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            val ai = context.packageManager.getApplicationInfo(packageName, 0)
            ai.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /*fun checkOverlayStatus() : Boolean {
        try {
            val pi = Class.forName("android.content.pm.PackageInfo")
            for (field : Field in pi.declaredFields) {
                if (field.name == "FLAG_OVERLAY_STATIC" || field.name == "FLAG_OVERLAY_TRUSTED") {
                    return true
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        return false
    }*/

    fun isSamsungOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getProperty("ro.config.knox", "def") != "def"
    }

    fun createImage(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun createLinkedString(ctx: Context, m: CharSequence, l: String): SpannableString {
        val click = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val url = ctx.getString(R.string.installer_source_link)
                val builder = CustomTabsIntent.Builder()
                val intent = builder.build()
                intent.launchUrl(ctx, Uri.parse(url))
            }
        }

        m.indexOf(l)

        val color = ForegroundColorSpan(ctx.swift.selection.accentColor)
        val ss = SpannableString(m)
        ss.setSpan(click, m.indexOf(l), m.indexOf(l) + l.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(color, m.indexOf(l), m.indexOf(l) + l.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        return ss
    }
}