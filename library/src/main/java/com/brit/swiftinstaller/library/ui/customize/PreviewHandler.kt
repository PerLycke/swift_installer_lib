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

package com.brit.swiftinstaller.library.ui.customize

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import kotlinx.android.synthetic.main.customize_preview_settings.view.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*

abstract class PreviewHandler(val context: Context) {

    var settingsPreview: ViewGroup? = null
    var systemUiPreview: ViewGroup? = null

    val settingsIcons = SynchronizedArrayList<ImageView>()
    val systemUiIcons = SynchronizedArrayList<ImageView>()

    open fun getPage(container: ViewGroup, position: Int): View {
        return if (position == 0) {
            settingsPreview =
                    LayoutInflater.from(context).inflate(R.layout.customize_preview_settings,
                            container, false) as ViewGroup

            settingsPreview?.let {
                settingsIcons.add(it.settings_connections_icon)
                settingsIcons.add(it.settings_sound_icon)
                settingsIcons.add(it.settings_notifications_icon)
            }

            settingsPreview!!
        } else {
            systemUiPreview = LayoutInflater.from(context).inflate(R.layout.customize_preview_sysui,
                    container, false) as ViewGroup

            systemUiPreview?.let {
                it.preview_wallpaper.clipToOutline = true

                systemUiIcons.add(it.systemui_wifi_icon)
                systemUiIcons.add(it.systemui_airplane_icon)
                systemUiIcons.add(it.systemui_bluetooth_icon)
                systemUiIcons.add(it.systemui_flashlight_icon)
                systemUiIcons.add(it.systemui_sound_icon)
                systemUiIcons.add(it.systemui_rotation_icon)
            }

            systemUiPreview!!
        }
    }

    open fun getPageCount(): Int {
        return 2
    }

    open fun updateIcons(selection: CustomizeSelection) {
    }

    open fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
        if (systemUiPreview == null || settingsPreview == null) return

        updateAccentColor(selection.accentColor)
        updateBackgroundColor(palette)
        updateIcons(selection)

        val darkNotif = (selection["notif_background"]) == "dark"
        val notifShadow = (selection["sender_name_fix"]) == "shadow"

        systemUiPreview?.let {
            if (notifShadow) {
                it.preview_sysui_msg.text =
                        context.getString(R.string.dark_notifications_preview_shadow)
                it.preview_sysui_sender.setTextColor(Color.BLACK)
                it.preview_sysui_sender.setShadowLayer(2.0f, -1.0f, -1.0f,
                        Color.WHITE)
            } else {
                it.preview_sysui_msg.text =
                        context.getString(R.string.dark_notifications_preview_normal)
                it.preview_sysui_sender.setTextColor(Color.WHITE)
                it.preview_sysui_sender.setShadowLayer(0.0f, 0.0f, 0.0f,
                        Color.TRANSPARENT)
            }
            it.notif_bg_layout.setImageResource(R.drawable.notif_bg)

            if (darkNotif) {
                if (notifShadow) {
                    it.preview_sysui_sender.setTextColor(Color.BLACK)
                } else {
                    it.preview_sysui_sender.setTextColor(Color.WHITE)
                }
                it.notif_bg_layout.drawable.setTint(palette.backgroundColor)
                it.preview_sysui_sender.text =
                        context.getString(R.string.dark_notifications)
                it.preview_sysui_msg.setTextColor(Color.parseColor("#b3ffffff"))
            } else {
                it.preview_sysui_sender.text =
                        context.getString(R.string.white_notifications)
                it.preview_sysui_msg.text =
                        context.getString(R.string.white_notifications_preview)
                it.notif_bg_layout.drawable.setTint(Color.parseColor("#f5f5f5"))
                it.preview_sysui_sender.setTextColor(Color.BLACK)
                it.preview_sysui_msg.setTextColor(Color.parseColor("#8a000000"))
            }
            it.qs_bg_layout.setImageResource(R.drawable.qs_bg)
            val qsAlpha = selection.getInt("qs_alpha")
            it.qs_bg_layout.drawable.setTint((ColorUtils.addAlphaColor(palette.backgroundColor, qsAlpha)))
        }
    }

    open fun updateAccentColor(accentColor: Int) {
        settingsPreview?.searchbar_search_icon?.setColorFilter(accentColor)
    }

    open fun updateBackgroundColor(palette: MaterialPalette) {
        if (settingsPreview == null || systemUiPreview == null) return

        settingsPreview?.let {
            if (it.settings_preview.drawable != null) {
                val settingsBackground =
                        it.settings_preview.drawable as LayerDrawable
                settingsBackground.findDrawableByLayerId(R.id.preview_background)
                        .setTint(palette.backgroundColor)
            }
            it.searchbar_bg.setColorFilter(palette.cardBackground)

        }
    }
}