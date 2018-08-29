package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.utils.OverlayUtils.getOverlayPackageName

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