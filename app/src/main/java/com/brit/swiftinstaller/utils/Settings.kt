package com.brit.swiftinstaller.utils

import android.content.Context
import android.preference.PreferenceManager

fun getAccentColor(context: Context): Int {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt("accent_color", 0)
}

fun setAccentColor(context: Context, color: Int) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("accent_color", color).apply()
}