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

package com.brit.swiftinstaller.library.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.*
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
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
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import kotlinx.android.synthetic.main.synergy_card_install.*
import org.jetbrains.anko.doAsync
import java.io.File


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

        if (Utils.isSynergyCompatibleDevice()) {
            File(Environment.getExternalStorageDirectory(), ".swift").deleteRecursively()
        }
        ChangelogHandler.showChangelog(this, true)

        update_checker_spinner.indeterminateDrawable.setColorFilter(swift.selection.accentColor,
                PorterDuff.Mode.SRC_ATOP)
        if (getProperty("ro.config.knox", "def") != "def") {
            if (Utils.isSynergyInstalled(this, "projekt.samsung.theme.compiler") && Utils.isSynergyCompatibleDevice()) {
                main_toolbar.subtitle = getString(R.string.main_toolbar_synergy_mode)
            } else if (ShellUtils.isRootAccessAvailable) {
                main_toolbar.subtitle = (getString(R.string.main_toolbar_root_mode))
            } else {
                main_toolbar.subtitle = (getString(R.string.main_toolbar_rootless_mode))
            }
        }

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

        synergy_card_install.setOnClickListener {
            val intent = Intent(this, SynergyActivity::class.java)
            startActivity(intent)
            synergy_card_install.isEnabled = false
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
        if (ShellUtils.isRootAccessAvailable) {
            MagiskUtils.createModule()
        }
    }

    private fun setupExtraCard() {
        if (prefs.getBoolean("foundExtra", false) || Utils.isSynergyCompatibleDevice()) {
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
            ).build(this, cards_list)
            cards_list.addView(extrasCard)
            extrasCardShowing = true
        }
    }

    private fun setupCards() {
        setupExtraCard()
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
            ).build(this), 0)
        }
        if (ShellUtils.isRootAccessAvailable && prefs.getBoolean("reboot_card", false)) {
            cards_list.addView(InfoCard(
                    desc = getString(R.string.info_card_reboot_msg),
                    icon = getDrawable(R.drawable.ic_reboot),
                    bgClick = View.OnClickListener {
                        val intent = Intent(this, RebootActivity::class.java)
                        startActivity(intent)
                    }
            ).build(this), 0)
            prefs.edit().putBoolean("reboot_card", false).apply()
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

        updateCard?.let {
            it.findViewById<TextView>(R.id.card_tip_count)?.visibility = View.INVISIBLE
            it.findViewById<ProgressBar?>(R.id.card_tip_spinner)?.visibility = View.VISIBLE
        }

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun updateFound() {
                if (!updateCardShowing) {
                    updateCard = MainCard(
                            title = getString(R.string.big_tile_install_updates),
                            desc = getString(R.string.big_tile_update_msg),
                            icon = getDrawable(R.drawable.ic_updates),
                            countTxt = getString(R.string.small_info_updates),
                            count = "...",
                            onClick = {
                                startActivity(Intent(this@MainActivity, OverlaysActivity::class.java)
                                        .putExtra("tab", OverlaysActivity.UPDATE_TAB))
                                updateCard?.isEnabled = false
                            }
                    ).build(this@MainActivity, cards_list)
                    cards_list.addView(updateCard, cards_list.indexOfChild(card_install))
                    updateCardShowing = true
                }
            }
            override fun finished(installedCount: Int, hasOption: Boolean, updates: SynchronizedArrayList<String>) {
                active_count.text = String.format("%d", installedCount)
                app_count.text = String.format("%d", installedCount)

                if (updates.isEmpty()) {
                    updateCard.let {
                        cards_list.removeView(it)
                    }
                } else {
                    updateCard?.let {
                        it.findViewById<TextView>(R.id.card_tip_count)?.text = String.format("%d", updates.size)
                        it.findViewById<ProgressBar?>(R.id.card_tip_spinner)?.visibility = View.INVISIBLE
                        it.findViewById<TextView>(R.id.card_tip_count)?.visibility = View.VISIBLE
                    }
                }

                if (!hasOption) {
                    if (extrasCardShowing && !Utils.isSynergyCompatibleDevice()) {
                        cards_list.removeView(extrasCard)
                        extrasCardShowing = false
                        prefs.edit().putBoolean("foundExtra", false).apply()
                    }
                } else if (!extrasCardShowing) {
                    prefs.edit().putBoolean("foundExtra", true).apply()
                    setupExtraCard()
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
        if (Utils.isSynergyInstalled(this, "projekt.samsung.theme.compiler") && Utils.isSynergyCompatibleDevice()) {
            synergy_card_install.isEnabled = true
            card_install.visibility = View.GONE
        } else {
            card_install.isEnabled = true
            synergy_card_install.visibility = View.GONE
        }
        card_personalize.isEnabled = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    fun overflowClick(view: View) {
        val popup = PopupWindow(this, null, 0, R.style.PopupWindow)
        val popupView = View.inflate(this, R.layout.popup_menu, null)
        if (!getUseSoftReboot()) {
            popupView.popup_menu_soft_reboot.text = getString(R.string.reboot)
        }
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.contentView = popupView
        popup.isFocusable = true

        val b = popupView.background as LayerDrawable
        b.findDrawableByLayerId(R.id.background_popup)
                .setTint(MaterialPalette.get(this).cardBackground)

        popupView.popup_menu_about.setOnClickListener {
            popup.dismiss()
            alert {
                title = getString(R.string.swift_app_name)

                val pi = packageManager.getPackageInfo(packageName, 0)
                val m = getString(R.string.swift_installer_version) + " ${pi.versionName}" + "\n\n" +
                        getString(R.string.swift_installer_lib_version) + " ${BuildConfig.VERSION_NAME}" +  " (${BuildConfig.VERSION_CODE})"  + "\n\n" +
                        getString(R.string.github)
                message = Utils.createLinkedString(this@MainActivity, m, getString(R.string.github), getString(R.string.link_installer_source))

                positiveButton(R.string.ok) { dialog ->
                    dialog.dismiss()
                }
                show()
            }
        }

        val click = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse(getString(R.string.email_support_intent_link))
                emailIntent.putExtra(Intent.EXTRA_EMAIL, Array(1) { getString(R.string.email_feedback) })
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_feedback_subject))
                startActivity(Intent.createChooser(emailIntent, getString(R.string.email_send_feedback)))
            }
        }

        popupView.popup_menu_help.setOnClickListener {
            popup.dismiss()

            alert {
                title = getString(R.string.help)

                val m = "${getString(R.string.help_msg)} \n\n" +
                        "${getString(R.string.documentation)} \n\n" +
                        "${getString(R.string.instructions)} \n\n" +
                        "${getString(R.string.telegram_support)} \n\n" +
                        "${getString(R.string.email_support)} \n\n" +
                        if (!MagiskUtils.magiskEnabled) {
                            "${getString(R.string.magisk_module)} \n\n"
                        } else {
                            ""
                        } +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && RomHandler.isSamsungPatched()) {
                            "${getString(R.string.rescue_zip_pie)} \n\n"
                        } else {
                            ""
                        } +
                        if (Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 27 || !RomHandler.isSamsungPatched()) {
                            "${getString(R.string.rescue_script)} \n\n"
                        } else {
                            ""
                        }


                        var mes = Utils.createLinkedString(ctx, m, getString(R.string.instructions), getString(R.string.link_instructions))
                        mes = Utils.createLinkedString(ctx, mes, getString(R.string.telegram_support), getString(R.string.link_telegram))
                        mes = Utils.createLinkedString(ctx, mes, getString(R.string.documentation), getString(R.string.link_documentation))
                        if (!MagiskUtils.magiskEnabled) {
                            mes = Utils.createLinkedString(ctx, mes, getString(R.string.magisk_module), getString(R.string.link_magisk))
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && RomHandler.isSamsungPatched()) {
                            mes = Utils.createLinkedString(ctx, mes, getString(R.string.rescue_zip_pie), getString(R.string.link_rescue_zip))
                        }
                        if (Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 27 || !RomHandler.isSamsungPatched()) {
                        mes = Utils.createLinkedString(ctx, mes, getString(R.string.rescue_script), getString(R.string.link_rescue_script))
                        }
                        val ss = SpannableString(mes)
                        ss.setSpan(click, m.indexOf(getString(R.string.email_support)), m.indexOf(getString(R.string.email_support)) + getString(R.string.email_support).length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        val color = ForegroundColorSpan(ctx.swift.selection.accentColor)
                        ss.setSpan(color, m.indexOf(getString(R.string.email_support)), m.indexOf(getString(R.string.email_support)) + getString(R.string.email_support).length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        message = ss

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

        if (ShellUtils.isRootAvailable && (!MagiskUtils.magiskEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.P)) {
            popupView.popup_menu_soft_reboot.setOnClickListener {
                popup.dismiss()
                val intent = Intent(this, RebootActivity::class.java)
                startActivity(intent)
            }
            if (getUseSoftReboot()) {
                popupView.popup_menu_soft_reboot.text = getString(R.string.soft_reboot)
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
