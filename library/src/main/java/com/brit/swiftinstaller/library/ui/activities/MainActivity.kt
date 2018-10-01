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
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.installer.rom.RomHandler
import com.brit.swiftinstaller.library.ui.InfoCard
import com.brit.swiftinstaller.library.ui.MainCard
import com.brit.swiftinstaller.library.ui.changelog.ChangelogHandler
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.countAvailableExtras
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import org.jetbrains.anko.doAsync


class MainActivity : ThemeActivity() {

    private val handler = Handler()
    private var updateCard: View? = null
    private var extrasCard: View? = null
    private var extrasCardShowing = false
    private var updateCardShowing = false
    private var extraApps: Set<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        extraApps = swift.extrasHandler.appExtras.keys

        ChangelogHandler.showChangelog(this, true)

        update_checker_spinner.indeterminateDrawable.setColorFilter(swift.selection.accentColor,
                PorterDuff.Mode.SRC_ATOP)

        doAsync {
            enableAllOverlays()
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

        setupCards()

        card_install.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            startActivity(intent)
            card_install.isEnabled = false
        }

        card_personalize.setOnClickListener {
            val intent = Intent(this, CustomizeActivity::class.java)
            startActivity(intent)
            card_personalize.isEnabled = false
        }

        if (Utils.isSamsungOreo()) {
            handler.post {
                val bootTime = SystemClock.elapsedRealtime()
                if (bootTime / 1000 / 60 < 4) {
                    if (!prefs.getBoolean("delay_on_boot_dialog", false)) {
                        prefs.edit().putBoolean("delay_on_boot_dialog", true).apply()
                        alert {
                            title = getString(R.string.reboot_delay_title)
                            message = getString(R.string.reboot_delay_msg)
                            positiveButton(R.string.got_it) { dialog ->
                                dialog.dismiss()
                            }
                            show()
                        }
                    }
                }
            }
        }
    }

    private fun setupCards() {
            if (prefs.getBoolean("not_closed", true)) {
                cards_list.addView(InfoCard(
                        desc = getString(R.string.info_card_compatibility_msg),
                        icon = getDrawable(R.drawable.ic_close),
                        btnClick = View.OnClickListener { view ->
                            prefs.edit()
                                    .putBoolean("not_closed", false).apply()
                            val parent = view.parent as View
                            parent.visibility = View.GONE
                        }
                ).build(this, cards_list), 0)
            }
            if (ShellUtils.isRootAccessAvailable && prefs.getBoolean("reboot_card", false)) {
                cards_list.addView(InfoCard(
                        desc = getString(R.string.info_card_reboot_msg),
                        icon = getDrawable(R.drawable.ic_reboot),
                        bgClick = View.OnClickListener {
                            val intent = Intent(this, RebootActivity::class.java)
                            startActivity(intent)
                        }
                ).build(this, cards_list), 0)
                prefs.edit().putBoolean("reboot_card", false).apply()
            }
    }

    private fun showExtraCard() {
        extrasCard = MainCard(
                title = getString(R.string.overlay_extras_title),
                desc = getString(R.string.extras_card_desc),
                icon = getDrawable(R.drawable.ic_extras),
                onClick = {
                    extrasCard?.isEnabled = false
                    extrasCard?.findViewById<ImageView>(R.id.card_new_indicator)?.setVisible(false)
                    prefs.edit().putBoolean("extras_indicator", false).apply()
                    startActivity(Intent(this, ExtrasActivity::class.java))
                }
        ).build(this@MainActivity, cards_list)
        cards_list.addView(extrasCard)
        extrasCardShowing = true
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

        updateCard?.let {
            it.findViewById<TextView>(R.id.card_tip_count)?.visibility = View.INVISIBLE
            it.findViewById<ProgressBar?>(R.id.card_tip_spinner)?.visibility = View.VISIBLE
        }

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, hasOption: Boolean, updates: SynchronizedArrayList<String>) {
                active_count.text = String.format("%d", installedCount)

                updateCard?.let {
                    it.findViewById<TextView>(R.id.card_tip_count)?.visibility = View.VISIBLE
                    it.findViewById<ProgressBar?>(R.id.card_tip_spinner)?.visibility = View.INVISIBLE
                }

                if (updates.isEmpty()) {
                    updateCard.let {
                        cards_list.removeView(it)
                    }
                } else if (!updateCardShowing) {
                    updateCard = MainCard(
                            title = getString(R.string.big_tile_install_updates),
                            desc = getString(R.string.big_tile_update_msg),
                            icon = getDrawable(R.drawable.ic_updates),
                            countTxt = getString(R.string.small_info_updates),
                            count = String.format("%d", updates.size),
                            onClick = {
                                startActivity(Intent(this@MainActivity, OverlaysActivity::class.java)
                                        .putExtra("tab", OverlaysActivity.UPDATE_TAB))
                                updateCard?.isEnabled = false
                            }
                    ).build(this@MainActivity, cards_list)
                    cards_list.addView(updateCard)
                    updateCardShowing = true
                } else {
                    updateCard?.findViewById<TextView>(R.id.card_tip_count)?.text = String.format("%d", updates.size)
                }

                if (!extrasCardShowing) {
                    if (hasOption ||
                            (extraApps != null && OverlayUtils.countAvailableExtras(this@MainActivity, extraApps!!) != 0)) {
                        showExtraCard()
                    }
                } else {
                    if (extraApps != null) {
                        val count = countAvailableExtras(this@MainActivity, extraApps!!)
                        if (count == 0 && !hasOption) {
                            if (extrasCard != null) {
                                cards_list.removeView(extrasCard)
                                extrasCardShowing = false
                            }
                        }
                    } else {
                        if (!hasOption) {
                            cards_list.removeView(extrasCard)
                            extrasCardShowing = false
                        }
                    }
                }

                extrasCard?.let {
                    if (prefs.getBoolean("extras_indicator", false)) {
                        it.findViewById<ImageView>(R.id.card_new_indicator)?.setVisible(true)
                    } else {
                        it.findViewById<ImageView>(R.id.card_new_indicator)?.setVisible(false)
                    }
                }
                update_checker_spinner.visibility = View.GONE
            }
        }).execute()

        extrasCard?.isEnabled = true
        updateCard?.isEnabled = true
        card_install.isEnabled = true
        card_personalize.isEnabled = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    fun overflowClick(view: View) {
        val popup = PopupWindow(this, null, 0, R.style.PopupWindow)
        val popupView = View.inflate(this, R.layout.popup_menu, null)
        if (!getUseSoftReboot(this)) {
            popupView.popup_menu_soft_reboot.text = getString(R.string.reboot)
        }
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.contentView = popupView
        popup.isFocusable = true

        val b = popupView.background as LayerDrawable
        b.findDrawableByLayerId(R.id.background_popup)
                .setTint(MaterialPalette.get(this).cardBackground)

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
                        if (RomHandler.supportsMagisk) {
                            "\n\n${getString(R.string.magisk_module,
                                    getString(R.string.link_magisk))} \n\n"
                        } else {
                            ""
                        } +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

        if (swift.romHandler.useHotSwap()) {
            popupView.popup_menu_soft_reboot.setOnClickListener {
                popup.dismiss()
                val intent = Intent(this, RebootActivity::class.java)
                startActivity(intent)
            }
        } else {
            popupView.popup_menu_soft_reboot.visibility = View.GONE
        }

        popupView.popup_menu_changelog.setOnClickListener {
            ChangelogHandler.showChangelog(this, false)
            popup.dismiss()
        }

        popup.showAtLocation(view, Gravity.TOP or Gravity.END, 0, 0)
    }
}
