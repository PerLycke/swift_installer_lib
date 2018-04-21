package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.getBackgroundColor

@SuppressLint("Registered")
open class ThemeActivity: AppCompatActivity() {
    private var mBlackBackground = false

    private val backgroundIDs = ArrayList<Int>()
    private val cardIDs = ArrayList<Int>()

    init {
        backgroundIDs.add(R.id.app_list_root)
        backgroundIDs.add(R.id.customize_root)
        backgroundIDs.add(R.id.installation_summary_root)
        backgroundIDs.add(R.id.my_toolbar)
        backgroundIDs.add(R.id.overlays_root)
        backgroundIDs.add(R.id.app_item_root)
        backgroundIDs.add(R.id.content_main_root)
        backgroundIDs.add(R.id.customize_accent_root)
        backgroundIDs.add(R.id.customize_bg_root)
        backgroundIDs.add(R.id.customize_preview_root)
        backgroundIDs.add(R.id.dialog_about_root)
        backgroundIDs.add(R.id.failed_info_card_root)
        backgroundIDs.add(R.id.palette_view_root)
        backgroundIDs.add(R.id.progress_dialog_root)
        backgroundIDs.add(R.id.sheet_confirm_root)
        backgroundIDs.add(R.id.sheet_install_root)
        backgroundIDs.add(R.id.sheet_overlays_fab_root)
        backgroundIDs.add(R.id.tab_install_summary_root)
        backgroundIDs.add(R.id.tabs_overlays_root)
        backgroundIDs.add(R.id.toolbar_install_summary_root)
        backgroundIDs.add(R.id.toolbar_customize_root)
        backgroundIDs.add(R.id.toolbar_overlays_root)

        cardIDs.add(R.id.install_updates_tile)
        cardIDs.add(R.id.install_tile)
        cardIDs.add(R.id.accent_tile)
        cardIDs.add(R.id.update_info)
        cardIDs.add(R.id.installed_info)
        cardIDs.add(R.id.accent_info)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mBlackBackground = getBackgroundColor(this) == 0x000000
        if (mBlackBackground) {
            setTheme(R.style.AppTheme_Black)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        for (id in backgroundIDs) {
            val v = findViewById<View>(id)
            if (v != null) {
                if (v is Toolbar) {
                    v.background = ColorDrawable(getBackgroundColor(this) - 0x050505)
                } else {
                    v.background = ColorDrawable(getBackgroundColor(this))
                }
            }
        }
        if (findViewById<FloatingActionButton>(R.id.fab) != null) {
            findViewById<FloatingActionButton>(R.id.fab).background.setTint(getAccentColor(this))
        }
        for (id in cardIDs) {
            val v = findViewById<View>(id)
            if (v != null) {
                if (v.background != null) {
                    v.background.setTint(getBackgroundColor(this) - 0xf7f7f8)
                } else if (v is ImageView && (v as ImageView).drawable != null) {
                    val iv = v as ImageView
                    Log.d("TEST", "class - " + iv.drawable.javaClass.name)
                    if (iv.drawable is StateListDrawable) {
                        val draw = iv.drawable as StateListDrawable
                        //draw.state[0]
                        draw.current.setTint(getBackgroundColor(this) - 0xf7f7f8)
                        //(v as ImageView).drawable.setTint(getBackgroundColor(this) - 0xf7f7f8)
                    } else if (iv.drawable is LayerDrawable) {
                        val draw = iv.drawable as LayerDrawable
                        draw.findDrawableByLayerId(R.id.background).setTint(getBackgroundColor(this) - 0xf7f7f8)
                        //draw.findDrawableByLayerId(R.id.stroke).setTint(getBackgroundColor(this) - 0xf7f7f8)
                    }
                }
            }
        }
    }
}