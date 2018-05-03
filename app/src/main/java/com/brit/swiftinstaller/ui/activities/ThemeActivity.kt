package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.*

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    private val backgroundIDs = ArrayList<Int>()
    private val cardIDs = ArrayList<Int>()

    init {
        backgroundIDs.add(R.id.app_list_root)
        backgroundIDs.add(R.id.customize_root)
        backgroundIDs.add(R.id.installation_summary_root)
        backgroundIDs.add(R.id.overlays_root)
        backgroundIDs.add(R.id.app_item_root)
        backgroundIDs.add(R.id.main_toolbar)
        backgroundIDs.add(R.id.content_main_root)
        backgroundIDs.add(R.id.customize_bg_root)
        backgroundIDs.add(R.id.customize_accent_root)
        backgroundIDs.add(R.id.customize_preview_root)
        backgroundIDs.add(R.id.failed_info_card_root)
        backgroundIDs.add(R.id.palette_view_root)
        backgroundIDs.add(R.id.tab_install_summary_root)
        backgroundIDs.add(R.id.tabs_overlays_root)
        backgroundIDs.add(R.id.toolbar_install_summary_root)
        backgroundIDs.add(R.id.toolbar_overlays_root)

        cardIDs.add(R.id.failed_info_card_layout)
        cardIDs.add(R.id.send_email_layout)
        cardIDs.add(R.id.popup_menu_root)
        cardIDs.add(R.id.card_update_bg)
        cardIDs.add(R.id.card_install_bg)
        cardIDs.add(R.id.card_personalize_bg)
        cardIDs.add(R.id.card_compatibility_bg)
        cardIDs.add(R.id.update_info)
        cardIDs.add(R.id.installed_info)
    }

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
        window.statusBarColor = palette.darkBackgroundColor
        window.navigationBarColor = palette.backgroundColor
        for (id in backgroundIDs) {
            val v = findViewById<View>(id)
            if (v != null) {
                if (v is Toolbar) {
                    v.background = ColorDrawable(palette.darkBackgroundColor)
                } else {
                    v.background = ColorDrawable(palette.backgroundColor)
                }
            }
        }
        if (findViewById<FloatingActionButton>(R.id.fab) != null) {
            findViewById<FloatingActionButton>(R.id.fab).background.setTint(getAccentColor(this))
        }
        for (id in cardIDs) {
            val v = findViewById<View>(id)
            Log.d("TEST", "id - ${resources.getResourceName(id)}")
            if (v != null) {
                Log.d("TEST", "not null")
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
            } else {
                Log.d("TEST", "null")
            }
            val popup = ContextCompat.getDrawable(this, R.drawable.popup_bg) as LayerDrawable
            popup.findDrawableByLayerId(R.id.background_popup).setTint(palette.cardBackgroud)
            val bottomSheet = ContextCompat.getDrawable(this, R.drawable.bottom_sheet_bg) as LayerDrawable
            bottomSheet.findDrawableByLayerId(R.id.background_bottom_sheet).setTint(palette.cardBackgroud)
            val failedInfoCard = ContextCompat.getDrawable(this, R.drawable.failed_info_card_bg) as LayerDrawable
            failedInfoCard.findDrawableByLayerId(R.id.background_failed_info_card).setTint(palette.cardBackgroud)
        }
    }
}