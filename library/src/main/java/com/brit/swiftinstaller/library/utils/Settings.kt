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
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.ArraySet
import androidx.collection.ArrayMap
import org.json.JSONObject

const val KEY_ACCENT_COLOR = "accent_color"
const val KEY_BACKGROUND_COLOR = "background_color"
const val KEY_BACKGROUND_PALETTE = "background_palette"
const val KEY_SENDER_NAME_FIX = "sender_name_fix"
const val KEY_USE_AOSP_ICONS = "use_aosp_icons"
const val KEY_USE_STOCK_ACCENT_ICONS = "use_stock_accent_icons"
const val KEY_USE_STOCK_MULTI_ICONS = "use_stock_multi_icons"
const val KEY_USE_P_ICONS = "use_p_icons"
const val KEY_USE_RIGHT_CLOCK = "use_right_clock"
const val KEY_USE_LEFT_CLOCK = "use_left_clock"
const val KEY_USE_CENTERED_CLOCK = "use_centered_clock"
const val KEY_USE_P_STYLE = "use_p_style"
const val KEY_HIDE_INFO_CARD = "hide_failed_info"
const val KEY_USER_ACCENTS = "user_accents"
const val KEY_OVERLAY_UPDATES = "overlays_to_update"
const val KEY_OVERLAYS_TO_INSTALL = "overlays_to_install"
const val KEY_OVERLAYS_TO_UNINSTALL = "overlays_to_uninstall"
const val KEY_DARK_NOTIF_BG = "dark_notif_bg"
const val KEY_ALPHA = "alpha"
const val KEY_USE_SOFT_REBOOT = "use_soft_reboot"
const val KEY_UPDATE_NOTIFICATION_ENABLED = "update_notification_enabled"
const val KEY_NEW_APP_NOTIFICATION_ENABLED = "new_app_notification_enabled"

const val KEY_HIDDEN_APPS = "hidden_apps"

val NO_PERMISSION_PACKAGES =
        arrayListOf("com.sec.android.app.music", "com.sec.android.app.voicenote")

fun useBackgroundPalette(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_BACKGROUND_PALETTE, false)
}

fun setUseBackgroundPalette(context: Context, use: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(KEY_BACKGROUND_PALETTE, use).apply()
}

fun setHideFailedInfoCard(context: Context, hide: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(KEY_HIDE_INFO_CARD, hide).apply()
}

fun getHideFailedInfoCard(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_HIDE_INFO_CARD, false)
}

fun getUserAccents(context: Context): IntArray {
    val temp =
            PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_USER_ACCENTS, "")!!
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

fun getHiddenApps(context: Context): Set<String> {
    return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(KEY_HIDDEN_APPS,
            emptySet()) ?: emptySet()
}

fun addHiddenApp(context: Context, packageName: String) {
    val apps = getHiddenApps(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_HIDDEN_APPS, apps.plus(packageName)).apply()
}

fun removeHiddenApp(context: Context, packageName: String) {
    val apps = getHiddenApps(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_HIDDEN_APPS, apps.minus(packageName)).apply()
}

fun getAppsToUpdate(context: Context): Set<String> {
    return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(KEY_OVERLAY_UPDATES,
            ArraySet<String>()) ?: emptySet()
}

fun addAppToUpdate(context: Context, packageName: String) {
    val apps = getAppsToUpdate(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAY_UPDATES, apps.plus(packageName)).apply()
}

fun removeAppToUpdate(context: Context, packageName: String) {
    val apps = getAppsToUpdate(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAY_UPDATES, apps.minus(packageName)).apply()
}

fun clearAppsToUpdate(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAY_UPDATES, ArraySet<String>()).apply()
}

fun getAppsToInstall(context: Context): Set<String> {
    return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            KEY_OVERLAYS_TO_INSTALL, ArraySet<String>()) ?: emptySet()
}

@Suppress("unused")
fun addAppToInstall(context: Context, packageName: String) {
    val apps = getAppsToInstall(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAYS_TO_INSTALL, apps.plus(packageName)).apply()
}

fun clearAppsToInstall(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAYS_TO_INSTALL, ArraySet<String>()).apply()
}

fun getAppsToUninstall(context: Context): Set<String> {
    return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            KEY_OVERLAYS_TO_UNINSTALL, ArraySet<String>()) ?: emptySet()
}

fun addAppToUninstall(context: Context, packageName: String) {
    val apps = getAppsToUninstall(context)
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAYS_TO_UNINSTALL, apps + packageName).apply()
}

fun clearAppsToUninstall(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putStringSet(KEY_OVERLAYS_TO_UNINSTALL, ArraySet<String>()).apply()
}

fun getUseSoftReboot(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_USE_SOFT_REBOOT, true)
}

fun updateNotificationEnabled(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            KEY_UPDATE_NOTIFICATION_ENABLED, false)
}

fun newAppNotificationEnabled(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            KEY_NEW_APP_NOTIFICATION_ENABLED, false)
}

fun setUserAccents(context: Context, colors: IntArray) {
    if (colors.size > 6) return
    val builder = StringBuilder()
    for (i in colors.indices) {
        if (i > 0) builder.append(",")
        builder.append(colors[i])
    }
    PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_USER_ACCENTS, builder.toString()).apply()
}

private fun getOverlayOptionsPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences("pref_app_options", Context.MODE_PRIVATE)
}

fun getSelectedOverlayOptions(context: Context, packageName: String): ArrayMap<String, String> {
    val map = ArrayMap<String, String>()
    val prefs = getOverlayOptionsPrefs(context)
    val jsonString = prefs.getString(packageName, JSONObject().toString())
    val json = JSONObject(jsonString)
    val iter = json.keys()
    while (iter.hasNext()) {
        val key = iter.next()
        val value = json.get(key)
        map[key] = value as String
    }
    return map
}

fun setOverlayOption(context: Context, packageName: String, option: String, value: String) {
    val prefs = getOverlayOptionsPrefs(context)
    val json = JSONObject(getSelectedOverlayOptions(context, packageName))
    json.put(option, value)
    prefs.edit().remove(packageName).apply()
    prefs.edit().putString(packageName, json.toString()).apply()
}

@Suppress("unused")
fun addAccentColor(context: Context, color: Int) {
    val presets = context.swift.romInfo.getCustomizeHandler().getAccentColors()
    for (col in presets) {
        if (col.accentColor == color)
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