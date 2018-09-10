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

package com.brit.swiftinstaller.library.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.installer.rom.RomInfo
import com.brit.swiftinstaller.library.ui.applist.AppItem
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays
import kotlinx.android.synthetic.main.card_compatibility_info.*
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.card_update.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.dialog_help.view.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import org.jetbrains.anko.doAsync

class MainActivity : ThemeActivity() {

    private var overlaysList = ArrayList<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        update_checker_spinner.indeterminateDrawable.setColorFilter(getAccentColor(this), PorterDuff.Mode.SRC_ATOP)

        doAsync {
            enableAllOverlays()
            overlaysList = Utils.sortedOverlaysList(this@MainActivity)
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("not_closed", true)) {
            card_compatibility.visibility = View.VISIBLE
            card_compatibility_close.setOnClickListener {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("not_closed", false).apply()
                card_compatibility.visibility = View.GONE
            }
        }

        if (ShellUtils.isRootAvailable &&
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("reboot_card", false)) {
            card_reboot.visibility = View.VISIBLE
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("reboot_card", false).apply()
            card_reboot.setOnClickListener {
                card_reboot.visibility = View.GONE
                val intent = Intent(this, RebootActivity::class.java)
                startActivity(intent)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)
            }
        }

        card_install.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList("overlays_list", overlaysList)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        card_update.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            intent.putExtra("tab", OverlaysActivity.UPDATE_TAB)
            val bundle = Bundle()
            bundle.putParcelableArrayList("overlays_list", overlaysList)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        card_personalize.setOnClickListener {
            val intent = Intent(this, CustomizeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0]
                                != PackageManager.PERMISSION_GRANTED)) {
                    // Permission denied show error dialog and exit
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onResume() {
        super.onResume()

        card_install.isClickable = false

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                active_count.text = String.format("%d", installedCount)
                update_checker_spinner.visibility = View.GONE
                card_install.isClickable = true
                if (updates.isEmpty()) {
                    card_update.visibility = View.GONE
                } else {
                    updates_count.text = String.format("%d", updates.size)
                    card_update.visibility = View.VISIBLE
                }
            }

        }).execute()
    }

    @SuppressLint("RtlHardcoded", "InflateParams")
    fun overflowClick(view: View) {
        val popup = PopupWindow(this, null, 0, R.style.PopupWindow)
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu, null)
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.contentView = popupView
        popup.isFocusable = true

        val b = popupView.background as LayerDrawable
        b.findDrawableByLayerId(R.id.background_popup).setTint(MaterialPalette.get(this).cardBackgroud)

        popupView.popup_menu_about.setOnClickListener { _ ->
            popup.dismiss()

            val dialogView = View.inflate(this, R.layout.dialog_about, null)
            val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
            builder.setView(dialogView)
            themeDialog()

            val dialog = builder.create()

            dialogView.about_ok_btn.setOnClickListener {
                dialog.dismiss()
            }

            dialogView.installer_version.text = BuildConfig.VERSION_NAME
            dialog.show()
        }

        popupView.popup_menu_help.setOnClickListener { _ ->
            popup.dismiss()

            val dialogView = View.inflate(this, R.layout.dialog_help, null)
            val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
            builder.setView(dialogView)
            themeDialog()

            val dialog = builder.create()

            dialogView.help_ok_btn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        popupView.popup_menu_settings.setOnClickListener {
            popup.dismiss()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        if (RomInfo.getRomInfo(this).useHotSwap()) {
            popupView.popup_menu_soft_reboot.setOnClickListener {
                popup.dismiss()
                rebootCommand()
            }
        } else {
            popupView.popup_menu_soft_reboot.visibility = View.GONE
        }

        popup.showAtLocation(view, Gravity.TOP or Gravity.RIGHT, 0, 0)
    }
}
