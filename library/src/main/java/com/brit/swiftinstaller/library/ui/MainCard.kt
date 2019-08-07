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
import android.view.ViewGroup
import android.widget.ImageView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.MaterialPalette
import kotlinx.android.synthetic.main.card_main.view.*

class MainCard(var title: String,
               var desc: String,
               var icon: Drawable?,
               var countTxt: String = "",
               var count: String = "",
               var onClick: () -> Unit) {

    fun build(context: Context, root: ViewGroup) : View {
        val cardLayout = LayoutInflater.from(context).inflate(R.layout.card_main, root, false)

        val bgIds = listOf(
                cardLayout.findViewById<ImageView>(R.id.card_bg).drawable as LayerDrawable,
                cardLayout.findViewById<ImageView>(R.id.card_tip_bg).drawable as LayerDrawable
        )
        bgIds.forEach {
            it.findDrawableByLayerId(R.id.background).setTint(MaterialPalette.get(context).cardBackground)
        }

        cardLayout.card_title.text = title
        cardLayout.card_desc.text = desc
        cardLayout.card_icon.setImageDrawable(icon)
        cardLayout.card_tip_desc.text = countTxt
        cardLayout.card_tip_count.text = count

        cardLayout.setOnClickListener {
            onClick()
        }

        if (countTxt != "") {
            cardLayout.card_tip_layout.visibility = View.VISIBLE
        }

        return cardLayout
    }
}