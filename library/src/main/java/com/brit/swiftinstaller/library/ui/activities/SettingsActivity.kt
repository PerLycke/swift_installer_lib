package com.brit.swiftinstaller.library.ui.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.installer.rom.RomHandler
import com.brit.swiftinstaller.library.utils.*
import com.topjohnwu.superuser.io.SuFile
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class SettingsActivity : AppCompatActivity() {

    private lateinit var palette: MaterialPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        palette = MaterialPalette.createPalette(swift.selection.backgroundColor, false)

        window.statusBarColor = palette.darkerBackgroundColor
        window.navigationBarColor = palette.darkerBackgroundColor

        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment())
                .commit()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(palette.darkBackgroundColor))
        supportActionBar!!.elevation = 0.0f
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)

            if (!activity!!.swift.romHandler.useHotSwap() || activity!!.swift.romHandler.magiskEnabled) {
                preferenceScreen.removePreference(
                        preferenceScreen.findPreference(KEY_USE_SOFT_REBOOT))
            } else {
                val softReboot = preferenceScreen.findPreference(KEY_USE_SOFT_REBOOT) as SwitchPreference
                softReboot.isChecked = getUseSoftReboot(activity!!)
            }

            val disableMagisk = preferenceScreen.findPreference("disable_magisk") as SwitchPreference
            disableMagisk.setDefaultValue(!SuFile(RomHandler.magiskPath).exists())
            disableMagisk.setOnPreferenceChangeListener { _, newValue ->
                val dlg = Utils.progressDialog(activity!!, activity!!.getString(R.string.overlay_move_msg))
                doAsync {
                    if ((newValue as Boolean)) {
                        MagiskUtils.convertFromMagisk(activity!!)
                    } else {
                        MagiskUtils.convertToMagisk(activity!!)
                    }
                    uiThread {
                        dlg.dismiss()
                    }
                }
                true
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