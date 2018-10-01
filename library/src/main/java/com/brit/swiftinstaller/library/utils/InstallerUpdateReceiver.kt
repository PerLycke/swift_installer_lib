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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import org.jetbrains.anko.doAsync

class InstallerUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        doAsync {
            OverlayUtils.checkAndHideOverlays(context)
            UpdateChecker(context, null).execute()
            convertSettings(context)
        }
    }

    private fun convertSettings(context: Context) {
        val selection = context.swift.romHandler.getCustomizeHandler().getSelection()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = prefs.edit()
        if (prefs.contains(KEY_ACCENT_COLOR)) {
            val accent = prefs.getInt(KEY_ACCENT_COLOR, 0)
            if (accent != 0) {
                selection.accentColor = accent
            }
            edit.remove(KEY_ACCENT_COLOR)
        }
        if (prefs.contains(KEY_BACKGROUND_COLOR)) {
            val background = prefs.getInt(KEY_BACKGROUND_COLOR, 0)
            if (background != 0) {
                selection.backgroundColor = background
            }
            edit.remove(KEY_BACKGROUND_COLOR)
        }
        if (prefs.contains(KEY_ALPHA)) {
            val alpha = prefs.getInt(KEY_ALPHA, -1)
            if (alpha != -1) {
                selection["qs_alpha"] = alpha.toString()
            }
            edit.remove(KEY_ALPHA)
        }

        fun checkSettingBoolean(settingsKey: String, selectionKey: String, value: String) {
            if (prefs.contains(settingsKey)) {
                val icons = prefs.getBoolean(settingsKey, false)
                if (icons) {
                    selection[selectionKey] = value
                }
                edit.remove(settingsKey)
            }
        }

        checkSettingBoolean(KEY_SENDER_NAME_FIX, "sender_name_fix", "shadow")
        checkSettingBoolean(KEY_USE_STOCK_ACCENT_ICONS, "samsung_oreo_icons", "stock_accent")
        checkSettingBoolean(KEY_USE_STOCK_MULTI_ICONS, "samsung_oreo_icons", "stock_multi")
        checkSettingBoolean(KEY_USE_P_ICONS, "samsung_oreo_icons", "p")
        checkSettingBoolean(KEY_USE_AOSP_ICONS, "samsung_oreo_icons", "aosp")
        checkSettingBoolean(KEY_USE_RIGHT_CLOCK, "samsung_oreo_clock", "right")
        checkSettingBoolean(KEY_USE_LEFT_CLOCK, "samsung_oreo_clock", "left")
        checkSettingBoolean(KEY_USE_CENTERED_CLOCK, "samsung_oreo_clock", "centered")
        checkSettingBoolean(KEY_USE_P_STYLE, "samsung_oreo_notif_style", "p")
        checkSettingBoolean(KEY_DARK_NOTIF_BG, "notif_background", "dark")

        edit.apply()
        context.swift.selection = selection
    }
}

