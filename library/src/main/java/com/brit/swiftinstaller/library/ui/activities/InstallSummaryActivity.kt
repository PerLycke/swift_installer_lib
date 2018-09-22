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

import android.app.Dialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.applist.AppItem
import com.brit.swiftinstaller.library.ui.applist.AppListFragment
import com.brit.swiftinstaller.library.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.library.utils.Holder
import com.brit.swiftinstaller.library.utils.OverlayUtils
import com.brit.swiftinstaller.library.utils.OverlayUtils.isOverlayEnabled
import com.brit.swiftinstaller.library.utils.ShellUtils
import com.brit.swiftinstaller.library.utils.alert
import com.brit.swiftinstaller.library.utils.getUseSoftReboot
import com.brit.swiftinstaller.library.utils.getVersionCode
import com.brit.swiftinstaller.library.utils.quickRebootCommand
import com.brit.swiftinstaller.library.utils.rebootCommand
import com.brit.swiftinstaller.library.utils.removeAppToUpdate
import com.brit.swiftinstaller.library.utils.restartSysUi
import com.brit.swiftinstaller.library.utils.swift
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_install_summary.*
import kotlinx.android.synthetic.main.tab_layout_install_summary.*
import org.jetbrains.anko.doAsync
import java.io.File

class InstallSummaryActivity : ThemeActivity() {

    companion object {
        private const val SUCCESS_TAB = 0
        const val FAILED_TAB = 1

        const val ACTION_INSTALL_CANCELLED = "com.brit.swiftinstaller.action.INSTALL_CANCELLED"
    }

    private lateinit var pagerAdapter: AppsTabPagerAdapter
    private val handler = Handler()

    private var errorMap: HashMap<String, String> = HashMap()
    private var apps = arrayListOf<String>()

    var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_summary)
        File(Environment.getExternalStorageDirectory(), ".swift").deleteRecursively()

        update = intent.getBooleanExtra("update", false)

        pagerAdapter = AppsTabPagerAdapter(supportFragmentManager, true, SUCCESS_TAB, FAILED_TAB)
        pagerAdapter.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                alert {
                    title = appItem.title
                    icon = appItem.icon
                    message = errorMap[appItem.packageName]!!
                    positiveButton(R.string.ok) { dialog ->
                        dialog.dismiss()
                    }
                    show()
                }
            }

        })

        container.adapter = pagerAdapter
        container.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(tab_install_summary_root))
        tab_install_summary_root.addOnTabSelectedListener(
                TabLayout.ViewPagerOnTabSelectedListener(container))

        //updateList()
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabFinishedClick(view: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_reboot, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(swift.selection.backgroundColor)
        bottomSheetDialog.show()

        val sendLog = sheetView.findViewById<View>(R.id.send_email_layout)
        val reboot = sheetView.findViewById<View>(R.id.reboot_layout)

        if (errorMap.isNotEmpty()) {
            sendLog.visibility = View.VISIBLE
            sendLog.setOnClickListener {
                sendErrorLog()
            }
        }

        if (pagerAdapter.getAppsCount(0) > 0) {
            reboot.visibility = View.VISIBLE
            reboot.setOnClickListener {
                bottomSheetDialog.dismiss()
                reboot()
            }
        }
    }

    private fun reboot() {
        val rebootDialog = Dialog(this, R.style.AppTheme_Translucent)
        rebootDialog.setContentView(R.layout.reboot)
        rebootDialog.show()
        handler.post {
            if (!swift.romInfo.magiskEnabled() && getUseSoftReboot(this)) {
                quickRebootCommand()
            } else {
                rebootCommand()
            }
        }
    }

    private fun updateList() {
        apps.clear()
        apps.addAll(Holder.installApps)
        errorMap.clear()
        errorMap.putAll(Holder.errorMap)
        pagerAdapter.clearApps()

        doAsync {
            val pm = this@InstallSummaryActivity.packageManager
            apps.addAll(errorMap.keys)
            apps.forEach { pn ->
                val info: ApplicationInfo?
                val pInfo: PackageInfo
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                } catch (ex: Exception) {
                    return@forEach
                }
                if (info != null) {
                    val item = AppItem(pn,
                            info.loadLabel(pm) as String,
                            pInfo.getVersionCode(),
                            pInfo.versionName,
                            info.loadIcon(pm))
                    if (errorMap.keys.contains(pn)) {
                        pagerAdapter.addApp(FAILED_TAB, item)
                    } else if (swift.romInfo.isOverlayInstalled(pn)) {
                        if (update && !OverlayUtils.wasUpdateSuccessful(this@InstallSummaryActivity,
                                        item.packageName)) {
                            errorMap[pn] = "Update Failed"
                            pagerAdapter.addApp(FAILED_TAB, item)
                        } else {
                            pagerAdapter.addApp(SUCCESS_TAB, item)
                            removeAppToUpdate(this@InstallSummaryActivity, item.packageName)
                        }
                    } else {
                        errorMap[pn] = "Install Cancelled"
                        LocalBroadcastManager.getInstance(
                                this@InstallSummaryActivity.applicationContext)
                                .sendBroadcast(Intent(ACTION_INSTALL_CANCELLED))
                        pagerAdapter.addApp(FAILED_TAB, item)
                    }
                }
            }
        }

        if (!ShellUtils.isRootAvailable && errorMap.isNotEmpty()) {
            send_email_layout.visibility = View.VISIBLE
            send_email_btn.setOnClickListener {
                sendErrorLog()
            }
        }

        val hotSwap =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hotswap", false)

        if (ShellUtils.isRootAvailable && !hotSwap) {
            fab_install_finished.show()
        }
        if (!hotSwap || !isOverlayEnabled("android")) {
            handler.post {
                resultDialog()
            }
        } else {
            restartSysUi()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("hotswap", false)
                    .apply()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateList()
    }

    private fun resultDialog() {
        val failed = apps.size == errorMap.size

        alert {
            title = when {
                failed -> getString(R.string.installation_failed)
                !ShellUtils.isRootAvailable -> getString(R.string.reboot_to_finish)
                else -> getString(R.string.reboot_now_title)
            }

            message = when {
                failed -> getString(R.string.examined_result_msg_error)
                errorMap.isNotEmpty() -> getString(R.string.examined_result_msg)
                else -> getString(R.string.examined_result_msg_noerror)
            }
            if (ShellUtils.isRootAvailable && !failed) {
                negativeButton(R.string.reboot_later) { dialog ->
                    dialog.dismiss()
                }
                positiveButton(R.string.reboot_now) { dialog ->
                    dialog.dismiss()
                    reboot()
                }
            } else {
                positiveButton(R.string.got_it) { dialog ->
                    dialog.dismiss()
                }
            }
            show()
        }

        if (!failed) {
            container.currentItem = 0
        } else {
            container.currentItem = 1
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun sendErrorLog() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, Array(1) { "swiftuserhelp@gmail.com" })
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Swift Installer: Error Log")

        val text = StringBuilder()
        text.append("\n")
        text.append("Installer Version: ${getString(R.string.lib_version)}")
        text.append("\n")
        text.append("Device: ${Build.DEVICE}")
        text.append("\n")
        text.append("Android Version: ${Build.VERSION.RELEASE}")
        text.append("\n")
        text.append("**********************************")
        text.append("\n")
        pagerAdapter.getApps(FAILED_TAB).forEach { item ->
            if (errorMap.containsKey(item.packageName)) {
                text.append("App: " + item.title)
                text.append("\n")
                text.append("App Package: " + item.packageName)
                text.append("\n")
                text.append("App Version: " + item.versionName)
                text.append("\n")
                text.append("Error Log: " + errorMap[item.packageName])
                text.append("\n")
                text.append("-------------------")
                text.append("\n")
            }
        }

        emailIntent.putExtra(Intent.EXTRA_TEXT, text.toString())
        startActivity(emailIntent)
    }
}
