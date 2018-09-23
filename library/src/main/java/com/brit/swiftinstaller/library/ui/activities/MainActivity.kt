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
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.changelog.ChangelogHandler
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays
import kotlinx.android.synthetic.main.card_compatibility_info.*
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.card_update.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import org.jetbrains.anko.doAsync

class MainActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        ChangelogHandler.showChangelog(this, true)

        update_checker_spinner.indeterminateDrawable.setColorFilter(swift.selection.accentColor,
                PorterDuff.Mode.SRC_ATOP)

        doAsync {
            enableAllOverlays()
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("not_closed", true)) {
            card_compatibility.visibility = View.VISIBLE
            card_compatibility_close.setOnClickListener {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("not_closed", false).apply()
                card_compatibility.visibility = View.GONE
            }
        }

        if (ShellUtils.isRootAccessAvailable &&
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("reboot_card",
                        false)) {
            card_reboot.visibility = View.VISIBLE
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("reboot_card", false).apply()
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
            startActivity(intent)
            card_install.isEnabled = false
        }

        card_update.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            intent.putExtra("tab", OverlaysActivity.UPDATE_TAB)
            startActivity(intent)
            card_update.isEnabled = false
        }

        card_personalize.setOnClickListener {
            val intent = Intent(this, CustomizeActivity::class.java)
            startActivity(intent)
            card_personalize.isEnabled = false
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

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: SynchronizedArrayList<String>) {
                active_count.text = String.format("%d", installedCount)
                update_checker_spinner.visibility = View.GONE
                if (updates.isEmpty()) {
                    card_update.visibility = View.GONE
                } else {
                    updates_count.text = String.format("%d", updates.size)
                    card_update.visibility = View.VISIBLE
                }
            }

        }).execute()

        card_install.isEnabled = true
        card_update.isEnabled = true
        card_personalize.isEnabled = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    @SuppressLint("RtlHardcoded", "InflateParams")
    fun overflowClick(view: View) {
        val popup = PopupWindow(this, null, 0, R.style.PopupWindow)
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu, null)
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.contentView = popupView
        popup.isFocusable = true

        val b = popupView.background as LayerDrawable
        b.findDrawableByLayerId(R.id.background_popup)
                .setTint(MaterialPalette.get(this).cardBackgroud)

        popupView.popup_menu_about.setOnClickListener { _ ->
            popup.dismiss()
            alert {
                title = getString(R.string.swift_app_name)

                val pi = packageManager.getPackageInfo(packageName, 0)
                val m = getString(R.string.installer_version, pi.versionName, pi.getVersionCode()) + "\n\n" +
                        getString(R.string.installer_library_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) + "\n\n" +
                        getString(R.string.installer_source_link)
                val l = getString(R.string.installer_source_link)

                message = Utils.createLinkedString(this@MainActivity, m, l)

                positiveButton(R.string.ok) { dialog ->
                    dialog.dismiss()
                }
                show()
            }
        }

        popupView.popup_menu_help.setOnClickListener { _ ->
            popup.dismiss()

            alert {
                title = getString(R.string.help)

                val m = "${getString(R.string.help_msg)} \n\n" +
                        "${getString(R.string.faq, getString(R.string.link_faq))} \n\n" +
                        getString(R.string.telegram_support, getString(R.string.link_telegram)) +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            "\n\n${getString(R.string.magisk_module,
                                    getString(R.string.link_magisk))} \n\n" +
                                    "${getString(R.string.rescue_zip_pie,
                                            getString(R.string.link_rescue_zip))} "
                        } else {
                            ""
                        }

                var mes = Utils.createLinkedString(ctx, m, getString(R.string.link_faq))
                mes = Utils.createLinkedString(ctx, mes, getString(R.string.link_telegram))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    mes = Utils.createLinkedString(ctx, mes, getString(R.string.link_magisk))
                    mes = Utils.createLinkedString(ctx, mes, getString(R.string.link_rescue_zip))
                }

                message = mes

                positiveButton(R.string.ok) { dialog ->
                    dialog.dismiss()
                }
                show()
            }
        }
        popupView.popup_menu_settings.setOnClickListener {
            popup.dismiss()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        if (swift.romInfo.useHotSwap()) {
            popupView.popup_menu_soft_reboot.setOnClickListener {
                popup.dismiss()
                if (getUseSoftReboot(this)) {
                    quickRebootCommand()
                } else {
                    rebootCommand()
                }
            }
        } else {
            popupView.popup_menu_soft_reboot.visibility = View.GONE
        }

        popupView.popup_menu_changelog.setOnClickListener {
            ChangelogHandler.showChangelog(this, false)
            popup.dismiss()
        }

        popup.showAtLocation(view, Gravity.TOP or Gravity.RIGHT, 0, 0)
    }
}
