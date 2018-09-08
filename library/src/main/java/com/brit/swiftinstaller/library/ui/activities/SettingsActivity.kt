package com.brit.swiftinstaller.library.ui.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.KEY_USE_SOFT_REBOOT
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.swift

class SettingsActivity : AppCompatActivity() {

    lateinit var palette: MaterialPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        palette = MaterialPalette.createPalette(swift.selection.backgroundColor, false)

        window.statusBarColor = palette.darkerBackgroundColor
        window.navigationBarColor = palette.darkerBackgroundColor

        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(palette.darkBackgroundColor))
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)

            if (!activity!!.swift.romInfo.useHotSwap()) {
                preferenceScreen.removePreference(
                        preferenceScreen.findPreference(KEY_USE_SOFT_REBOOT))
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.setBackgroundColor(activity!!.swift.selection.backgroundColor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}