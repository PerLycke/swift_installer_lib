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

import com.brit.swiftinstaller.library.R

object IdLists {

    val radioButtons = listOf(
            R.id.material_theme,
            R.id.flat_theme
    )

    val bgIds = arrayListOf(
            R.id.content_main_root,
            R.id.main_toolbar,
            R.id.customize_root,
            R.id.installation_summary_root,
            R.id.overlays_root,
            R.id.customize_bg_root,
            R.id.customize_accent_root,
            R.id.customize_preview_root,
            R.id.palette_view_root,
            R.id.tab_install_summary_root,
            R.id.tabs_overlays_root,
            R.id.activity_extras_root
    )

    val cardIds = arrayListOf(
            R.id.failed_info_card_layout,
            R.id.send_email_layout,
            R.id.card_install_bg,
            R.id.card_personalize_bg,
            R.id.card_compatibility_bg,
            R.id.card_reboot_bg,
            R.id.installed_info,
            R.id.card_bg,
            R.id.card_tip_bg
    )
}