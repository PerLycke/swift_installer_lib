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

package com.brit.swiftinstaller.utils

import com.brit.swiftinstaller.R

object IdLists {

    val radioButtons = listOf(
            R.id.material_theme,
            R.id.flat_theme,
            R.id.aosp_icons,
            R.id.stock_icons,
            R.id.stock_icons_multi,
            R.id.p_icons,
            R.id.white_notifications,
            R.id.dark_notifications,
            R.id.shadow_enabled,
            R.id.shadow_disabled,
            R.id.right_clock,
            R.id.left_clock,
            R.id.centered_clock,
            R.id.default_style,
            R.id.p_style
    )

    val bgIndicators = listOf(
            R.id.custom_dark_bg_indicator,
            R.id.custom_black_bg_indicator,
            R.id.custom_style_bg_indicator,
            R.id.custom_nature_bg_indicator,
            R.id.custom_ocean_bg_indicator,
            R.id.custom_night_bg_indicator
    )

    val bgIds = arrayListOf(
            R.id.customize_root,
            R.id.installation_summary_root,
            R.id.overlays_root,
            R.id.main_toolbar,
            R.id.content_main_root,
            R.id.customize_bg_root,
            R.id.customize_accent_root,
            R.id.customize_preview_root,
            R.id.palette_view_root,
            R.id.tab_install_summary_root,
            R.id.tabs_overlays_root,
            R.id.toolbar_install_summary_root,
            R.id.toolbar_overlays_root

    )

    val cardIds = arrayListOf(
            R.id.failed_info_card_layout,
            R.id.send_email_layout,
            R.id.card_update_bg,
            R.id.card_install_bg,
            R.id.card_personalize_bg,
            R.id.card_compatibility_bg,
            R.id.card_reboot_bg,
            R.id.update_info,
            R.id.installed_info
    )
}