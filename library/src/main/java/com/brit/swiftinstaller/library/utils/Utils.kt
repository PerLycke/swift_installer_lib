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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import com.brit.swiftinstaller.library.installer.rom.RomInfo
import com.brit.swiftinstaller.library.ui.applist.AppItem
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName

object Utils {

    private val sortedOverlays = arrayListOf<AppItem>()

    fun sortedOverlaysList(context: Context): ArrayList<AppItem> {
        if (sortedOverlays.isNotEmpty()) return sortedOverlays
        sortedOverlays.clear()
        val disabledOverlays = RomInfo.getRomInfo(context).getDisabledOverlays()
        val hiddenOverlays = getHiddenApps(context)
        val pm = context.packageManager
        val overlays = context.assets.list("overlays") ?: emptyArray()
        for (pn: String in overlays) {
            if (disabledOverlays.contains(pn)) continue
            if (hiddenOverlays.contains(pn)) continue
            var info: ApplicationInfo?
            var pInfo: PackageInfo?
            try {
                info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                pInfo = pm.getPackageInfo(pn, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
            if (info != null) {
                if (!info.enabled) continue
                val item = AppItem()
                item.packageName = pn
                item.title = info.loadLabel(pm) as String
                item.versionCode = pInfo!!.getVersionCode()
                item.versionName = pInfo.versionName
                sortedOverlays.add(item)
            }
        }
        sortedOverlays.sortWith(Comparator { o1, o2 ->
            o1.title.compareTo(o2.title)
        })
        return sortedOverlays
    }

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

    fun checkAppVersion(context: Context, packageName: String): Boolean {
        if (!isAppInstalled(context, getOverlayPackageName(packageName))) return false
        val appVersionCode = context.packageManager.getPackageInfo(packageName, 0).getVersionCode()
        val curVersionCode = context.packageManager.getApplicationInfo(
                getOverlayPackageName(packageName),
                PackageManager.GET_META_DATA).metaData.getInt("app_version_code")
        return appVersionCode > curVersionCode
    }

    fun createImage(width:Int, height:Int, color:Int):Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}