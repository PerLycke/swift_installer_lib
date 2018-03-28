package com.brit.swiftinstaller.utils

import android.content.Context
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.ArraySet
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.rom.RomInfo

fun getAccentColor(context: Context): Int {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt("accent_color", RomInfo.getRomInfo(context).defaultAccent)
}

fun setAccentColor(context: Context, color: Int) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("accent_color", color).apply()
}

fun getUserAccents(context: Context): IntArray {
    val temp = PreferenceManager.getDefaultSharedPreferences(context).getString("accents", "")
    if (TextUtils.isEmpty(temp)) {
        return IntArray(0)
    }
    val col = temp.split(",")
    val accents = IntArray(col.size)
    col.indices
            .filterNot { TextUtils.isEmpty(col[it]) }
            .forEach { accents[it] = Integer.parseInt(col[it]) }
    return accents
}

fun getAppsToInstall(context: Context): Set<String> {
    return PreferenceManager.getDefaultSharedPreferences(context).getStringSet("overlays_to_install", ArraySet<String>())
}

fun addAppToInstall(context: Context, packageName: String) {
    val apps = getAppsToInstall(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet("apps", apps.plus(packageName)).apply()
}

fun setUserAccents(context: Context, colors: IntArray) {
    if (colors.size > 6) return
    val builder = StringBuilder()
    for (i in colors.indices) {
        if (i > 0) builder.append(",")
        builder.append(colors[i])
    }
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("accents", builder.toString()).apply()
}

@Suppress("unused")
fun addAccentColor(context: Context, color: Int) {
    val presets = context.resources.getIntArray(R.array.accent_colors)
    for (col: Int in presets) {
        if (col == color)
            return
    }
    val colors = getUserAccents(context)
    val newColors: IntArray
    if (colors.size == 6) {
        colors[0] = color
        newColors = colors
    } else {
        newColors = IntArray(colors.size + 1)
        newColors[0] = color
        for (i in colors.indices) {
            newColors[i + 1] = colors[i]
        }
    }
    setUserAccents(context, newColors)
}

fun setKnoxKey(context: Context, key: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("knox_key", key).apply()
}

fun getKnoxKey(context: Context) : String {
    return PreferenceManager.getDefaultSharedPreferences(context).getString("knox_key", "")
}

fun setEnterpriseKey(context: Context, key: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("enterprise_key", key).apply()
}

fun getEnterpriseKey(context: Context) : String {
    return PreferenceManager.getDefaultSharedPreferences(context).getString("enterprise_key", "")
}