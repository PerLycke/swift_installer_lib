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
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.CardItem
import com.brit.swiftinstaller.library.ui.MainCard
import com.brit.swiftinstaller.library.ui.changelog.ChangelogHandler
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.card_item.view.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import org.jetbrains.anko.doAsync


class MainActivity : ThemeActivity() {

    private val handler = Handler()
    private var updateCard: View? = null
    private var updateCardShowing = false

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

        setupCards()

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
                            positiveButton(R.string.proceed) { dialog ->
                                dialog.dismiss()
                                startActivity(intent)
                            }
                            show()
                        }
                    }
                }
            }
        }
    }

    private fun setupCards() {
        val idleHandler = MessageQueue.IdleHandler {
            val cardsList = arrayListOf<CardItem>()
            val cardView = cards_list

            if (prefs.getBoolean("not_closed", true)) {
                cardsList.add(CardItem(
                        getString(R.string.info_card_compatibility_msg),
                        getDrawable(R.drawable.ic_close),
                        View.OnClickListener { view ->
                            prefs.edit()
                                    .putBoolean("not_closed", false).apply()
                            val parent = view.parent as View
                            parent.visibility = View.GONE
                        },
                        null
                ))
            }
            if (ShellUtils.isRootAccessAvailable && prefs.getBoolean("reboot_card", false)) {
                cardsList.add(CardItem(
                        getString(R.string.info_card_reboot_msg),
                        getDrawable(R.drawable.ic_reboot),
                        null,
                        View.OnClickListener {
                            val intent = Intent(this, RebootActivity::class.java)
                            startActivity(intent)
                        }
                ))
                prefs.edit().putBoolean("reboot_card", false).apply()
            }

            cardsList.forEach { item ->
                val cardItemLayout = LayoutInflater.from(this@MainActivity).inflate(R.layout.card_item, null)
                cardItemLayout.card_item_desc.text = item.desc
                cardItemLayout.card_item_btn.setImageDrawable(item.btn)
                cardItemLayout.card_item_btn.setOnClickListener(item.btnClick)
                cardItemLayout.card_item_root.setOnClickListener(item.bgClick)
                cardView.addView(cardItemLayout, 0)
            }
            false
        }
        Looper.myQueue().addIdleHandler(idleHandler)

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
            override fun finished(installedCount: Int, updates: SynchronizedArrayList<String>) {
                active_count.text = String.format("%d", installedCount)
                update_checker_spinner.visibility = View.GONE
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
                    ).build(this@MainActivity)
                    cards_list.addView(updateCard, 0)
                    updateCardShowing = true
                } else {
                    updateCard?.findViewById<TextView>(R.id.card_tip_count)?.text = String.format("%d", updates.size)
                }
            }
        }).execute()

        updateCard?.isEnabled = true
        card_install.isEnabled = true
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

        popup.showAtLocation(view, Gravity.TOP or Gravity.RIGHT, 0, 0)
    }
}
