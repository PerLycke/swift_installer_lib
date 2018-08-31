package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.*

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ACCENT_COLOR || key == KEY_BACKGROUND_COLOR || key == KEY_BACKGROUND_PALETTE) {
                updateColors(getBackgroundColor(this), useBackgroundPalette(this))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateColors(getBackgroundColor(this), useBackgroundPalette(this))
    }

    fun themeDialog() {
        val dialogBg = getDrawable(R.drawable.dialog_bg) as LayerDrawable
        dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(getBackgroundColor(this))
    }

    fun updateColors(backgroundColor: Int, usePalette: Boolean) {
        val palette = MaterialPalette.createPalette(backgroundColor, usePalette)
        window.statusBarColor = palette.backgroundColor
        window.navigationBarColor = palette.backgroundColor
        for (id in IdLists.bgIds) {
            val v = findViewById<View>(id)
            if (v != null) {
                v.background = ColorDrawable(palette.backgroundColor)
            }
        }
        if (findViewById<FloatingActionButton>(R.id.fab) != null) {
            findViewById<FloatingActionButton>(R.id.fab).background.setTint(getAccentColor(this))
        }
        for (id in IdLists.cardIds) {
            val v = findViewById<View>(id)
            if (v != null) {
                if (v.background != null) {
                    v.background.setTint(palette.cardBackgroud)
                } else if (v is ImageView && v.drawable != null) {
                    if (v.drawable is StateListDrawable) {
                        val draw = v.drawable as StateListDrawable
                        draw.current.setTint(palette.cardBackgroud)
                    } else if (v.drawable is LayerDrawable) {
                        val draw = v.drawable as LayerDrawable
                        draw.findDrawableByLayerId(R.id.background).setTint(palette.cardBackgroud)
                    }
                }
            }
        }
    }
}