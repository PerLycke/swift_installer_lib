package com.brit.swiftinstaller.library.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.MaterialPalette
import kotlinx.android.synthetic.main.main_card.view.*

class MainCard(var title: String,
               var desc: String,
               var icon: Drawable?,
               var countTxt: String = "",
               var count: String = "",
               var onClick: () -> Unit) {

    fun build(context: Context) : View {
        val cardLayout = LayoutInflater.from(context).inflate(R.layout.main_card, null)

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