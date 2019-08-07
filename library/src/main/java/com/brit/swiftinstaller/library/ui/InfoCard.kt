/*
 *
 *  * Copyright (C) 2019 Griffin Millender
 *  * Copyright (C) 2019 Per Lycke
 *  * Copyright (C) 2019 Davide Lilli & Nishith Khanna
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

package com.brit.swiftinstaller.library.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.MaterialPalette
import kotlinx.android.synthetic.main.card_info.view.*

class InfoCard(val desc: String,
               val icon: Drawable?,
               val btnClick: View.OnClickListener? = null,
               val bgClick: View.OnClickListener? = null) {

    fun build(context: Context) : View {
        val cardView = LayoutInflater.from(context).inflate(R.layout.card_info, null)

        val bg = cardView.findViewById<ImageView>(R.id.card_bg).drawable as LayerDrawable
        bg.findDrawableByLayerId(R.id.background).setTint(MaterialPalette.get(context).cardBackground)

        cardView.card_item_desc.text = desc
        cardView.card_item_btn.setImageDrawable(icon)

        if (btnClick != null) {
            cardView.card_item_btn.setOnClickListener(btnClick)
        }

        if (bgClick != null) {
            cardView.card_item_root.setOnClickListener(bgClick)
        }

        return cardView
    }
}