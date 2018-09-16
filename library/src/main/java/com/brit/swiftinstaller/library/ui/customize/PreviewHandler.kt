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
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.setVisible
import kotlinx.android.synthetic.main.customize_preview_settings.view.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*

abstract class PreviewHandler(val context: Context) {

    private val settingsPreview: ViewGroup =
            View.inflate(context, R.layout.customize_preview_settings, null) as ViewGroup
    private val systemUiPreview: ViewGroup =
            View.inflate(context, R.layout.customize_preview_sysui, null) as ViewGroup

    val settingsIcons = ArrayList<ImageView>()
    val systemUiIcons = ArrayList<ImageView>()
    init {
        settingsIcons.add(settingsPreview.settings_connections_icon)
        settingsIcons.add(settingsPreview.settings_sound_icon)
        settingsIcons.add(settingsPreview.settings_notifications_icon)
        systemUiIcons.add(systemUiPreview.systemui_wifi_icon)
        systemUiIcons.add(systemUiPreview.systemui_airplane_icon)
        systemUiIcons.add(systemUiPreview.systemui_bluetooth_icon)
        systemUiIcons.add(systemUiPreview.systemui_flashlight_icon)
        systemUiIcons.add(systemUiPreview.systemui_sound_icon)
        systemUiIcons.add(systemUiPreview.systemui_rotation_icon)
    }

    open fun getPage(container: ViewGroup, position: Int) : View {
        return if (position == 0) {
            settingsPreview
        } else {
            systemUiPreview
        }
    }

    open fun getPageCount(): Int {
        return 2
    }

    open fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
        updateAccentColor(selection.accentColor)
        updateBackgroundColor(palette)
        for (icon in settingsIcons) {
                val type: String = when {
                    selection["icons"] == "aosp" -> {
                        icon.setColorFilter(selection.accentColor)
                        "aosp"
                    }
                    selection["icons"] == "p" -> {
                        icon.clearColorFilter()
                        "p"
                    }
                    selection["icons"] == "stock_multi" -> {
                        icon.clearColorFilter()
                        "stock"
                    }
                    else -> {
                        icon.setColorFilter(selection.accentColor)
                        "stock"
                    }
                }

                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_$type"
                val id = context.resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
        }

        for (icon in systemUiIcons) {
                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_${selection["icons"]}}"
                val id = context.resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
        }

        val qsAlpha = (selection["qsAlpha"])
        systemUiPreview.preview_wallpaper.setColorFilter(
                ColorUtils.addAlphaColor(palette.backgroundColor,
                        Integer.parseInt(qsAlpha)), PorterDuff.Mode.SRC_OVER)


        if (selection.containsKey("clock")) {
            val clockSelection = selection["clock"]
            settingsPreview.clock_right.setVisible(clockSelection == "right")
            settingsPreview.clock_left.setVisible(clockSelection == "left")
            settingsPreview.clock_centered.setVisible(clockSelection == "centered")
        }

        val darkNotif = (selection["notif_background"]) == "dark"
        val notifShadow = (selection["shadow_fix"]) == "shadow"
        val usePStyle = (selection["style"]) == "p"

        if (notifShadow) {
            systemUiPreview.preview_sysui_msg.text = context.getString(R.string.dark_notifications_preview_shadow)
            systemUiPreview.preview_sysui_app_title.setShadowLayer(2.0f, -1.0f, -1.0f, Color.WHITE)
            systemUiPreview.preview_sysui_sender.setTextColor(Color.BLACK)
            systemUiPreview.preview_sysui_sender.setShadowLayer(2.0f, -1.0f, -1.0f, Color.WHITE)
        } else {
            systemUiPreview.preview_sysui_msg.text = context.getString(R.string.dark_notifications_preview_normal)
            systemUiPreview.preview_sysui_app_title.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
            systemUiPreview.preview_sysui_sender.setTextColor(Color.WHITE)
            systemUiPreview.preview_sysui_sender.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
        }

        if (usePStyle) {
            systemUiPreview.notif_bg_layout.setImageResource(R.drawable.notif_bg_rounded)
        } else {
            systemUiPreview.notif_bg_layout.setImageResource(R.drawable.notif_bg)
        }

        if (darkNotif) {
            if (notifShadow) {
                systemUiPreview.preview_sysui_sender.setTextColor(Color.BLACK)
            } else {
                systemUiPreview.preview_sysui_sender.setTextColor(Color.WHITE)
            }
            if (usePStyle) {
                systemUiPreview.notif_bg_layout.drawable.setTint(ColorUtils.handleColor(palette.backgroundColor, 8))
            } else {
                systemUiPreview.notif_bg_layout.drawable.setTint(palette.backgroundColor)
            }
            systemUiPreview.preview_sysui_sender.text = context.getString(R.string.dark_notifications)
            systemUiPreview.preview_sysui_msg.setTextColor(Color.parseColor("#b3ffffff"))
        } else {
            systemUiPreview.preview_sysui_sender.text =
                    context.getString(R.string.white_notifications)
            systemUiPreview.preview_sysui_msg.text =
                    context.getString(R.string.white_notifications_preview)
            systemUiPreview.notif_bg_layout.drawable.setTint(Color.parseColor("#f5f5f5"))
            systemUiPreview.preview_sysui_sender.setTextColor(Color.BLACK)
            systemUiPreview.preview_sysui_msg.setTextColor(Color.parseColor("#8a000000"))
        }
    }

    open fun updateAccentColor(accentColor: Int) {
    }

    open fun updateBackgroundColor(palette: MaterialPalette) {

        if (settingsPreview.settings_preview.drawable != null) {
            val settingsBackground = settingsPreview.settings_preview.drawable as LayerDrawable
            settingsBackground.findDrawableByLayerId(R.id.preview_background).setTint(palette.backgroundColor)
        }
        if (systemUiPreview.preview_sysui_bg.drawable != null) {
            val sysUiBackground = systemUiPreview.preview_sysui_bg.drawable as LayerDrawable
            sysUiBackground.findDrawableByLayerId(R.id.preview_background).setTint(palette.backgroundColor)
        }
        Log.d("TEST", "backgroundColor - ${palette.backgroundColor}")
        Log.d("TEST", "cardBackground - ${palette.cardBackgroud}")
        settingsPreview.searchbar_bg.setColorFilter(palette.cardBackgroud)
        settingsPreview.searchbar_bg.invalidate()
    }
}