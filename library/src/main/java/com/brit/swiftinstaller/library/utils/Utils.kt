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
import android.content.Intent
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
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.content.pm.PackageManager
import com.brit.swiftinstaller.library.installer.rom.RomHandler


object Utils {

    fun isSamsungOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getProperty("ro.config.knox", "def") != "def"
    }

    fun isSynergyInstalled(context: Context, applicationId: String): Boolean {
        try {
            context.packageManager.getPackageInfo(applicationId, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }
    
    fun isSynergyCompatibleDevice(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getProperty("ro.config.knox", "def") != "def" && RomHandler.isSamsungPatched() && !ShellUtils.isRootAccessAvailable
    }

    fun createImage(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun createLinkedString(ctx: Context, message: CharSequence, urltext: String, urllink: String): SpannableString {
        return createLinkedString(ctx, message, message.indexOf(urltext), message.indexOf(urltext) + urltext.length, urllink)
    }

    fun createLinkedString(ctx: Context, message: CharSequence, start: Int, end: Int, urllink: String): SpannableString {
        val click = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(ctx.swift.selection.backgroundColor)
                builder.setSecondaryToolbarColor(ctx.swift.selection.backgroundColor)
                builder.setShowTitle(false)
                builder.enableUrlBarHiding()
                val intent = builder.build()
                intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.launchUrl(ctx, Uri.parse(urllink))
            }
        }

        val color = ForegroundColorSpan(ctx.swift.selection.accentColor)
        val ss = SpannableString(message)
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(color, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        return ss
    }

    fun zip(files: Array<File>, zipFileName: String) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFileName))).use { out ->
            for (file in files) {
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entry = ZipEntry(file.name)
                        out.putNextEntry(entry)
                        origin.copyTo(out, 1024)
                    }
                }
            }
        }
    }
}

fun setContainsAny(set: Set<String>, array: Array<String>): Boolean {
    array.forEach { if (set.contains(it)) return true }
    return false
}