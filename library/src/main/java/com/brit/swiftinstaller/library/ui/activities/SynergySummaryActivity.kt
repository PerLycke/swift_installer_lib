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

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.view.View
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.InfoCard
import com.brit.swiftinstaller.library.ui.applist.AppItem
import com.brit.swiftinstaller.library.ui.applist.AppListFragment
import com.brit.swiftinstaller.library.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.library.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_install_summary.*
import kotlinx.android.synthetic.main.tab_install_summary_failed.*
import kotlinx.android.synthetic.main.tab_install_summary_success.*
import kotlinx.android.synthetic.main.tab_layout_install_summary.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class SynergySummaryActivity : ThemeActivity() {

    companion object {
        private const val SUCCESS_TAB = 0
        const val FAILED_TAB = 1
    }

    private lateinit var pagerAdapter: AppsTabPagerAdapter

    private var errorMap: HashMap<String, String> = HashMap()
    private var apps = arrayListOf<String>()
    private var success = false
    private var failed = false

    var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_summary)

        pagerAdapter = AppsTabPagerAdapter(supportFragmentManager, true, false, SUCCESS_TAB, FAILED_TAB)
        pagerAdapter.retainInstance()
        pagerAdapter.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                alert {
                    title = appItem.title
                    icon = appItem.icon
                    message = "Installer Version: ${getString(R.string.lib_version)}\n" + "App Version: " + appItem.versionName + "\n\n" + errorMap[appItem.packageName]!!
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
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabFinishedClick(view: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_synergy, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(swift.selection.backgroundColor)
        bottomSheetDialog.show()

        val sendLog = sheetView.findViewById<View>(R.id.send_email_synergy_layout)
        val goToSynergy = sheetView.findViewById<View>(R.id.go_to_synergy_layout)

        if (Holder.errorMap.isNotEmpty() && !setContainsAny(Holder.errorMap.keys, resources.getStringArray(R.array.log_blacklist))) {
            sendLog.visibility = View.VISIBLE
            sendLog.setOnClickListener {
                sendErrorLog()
            }
        }
            goToSynergy.visibility = View.VISIBLE
            goToSynergy.setOnClickListener {
                swift.romHandler.postInstall(false, SynchronizedArrayList(intent.getStringArrayListExtra("app list")))
            }
    }

    private fun updateList() {
        apps.clear()
        apps.addAll(Holder.installApps)
        errorMap.clear()
        errorMap.putAll(Holder.errorMap)
        pagerAdapter.clearApps()

        doAsync {
            val pm = this@SynergySummaryActivity.packageManager
            val failedList = arrayListOf<AppItem>()
            val successList = arrayListOf<AppItem>()

            apps.plus(errorMap.keys).forEach { pn ->
                val info: ApplicationInfo?
                val pInfo: PackageInfo
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                } catch (ex: Exception) {
                    return@forEach
                }
                if (info != null) {
                    val item = AppItem(packageName = pn,
                            title = info.loadLabel(pm) as String,
                            versionCode = pInfo.getVersionCode(),
                            versionName = pInfo.versionName,
                            icon = info.loadIcon(pm))
                    if (errorMap.keys.contains(pn)) {
                        failedList.add(item)
                    } else {
                        successList.add(item)
                    }
                }
            }

            uiThread {

                successList.forEach { item ->
                    pagerAdapter.addApp(SUCCESS_TAB, item)
                }
                failedList.forEach { item ->
                    pagerAdapter.addApp(FAILED_TAB, item)
                }

                success = failedList.isEmpty()
                failed = successList.isEmpty()

                if (failedList.isEmpty()) {
                        send_email_layout.visibility = View.VISIBLE
                    send_email_btn.text = getString(R.string.go_to_synergy)
                    send_email_btn.setTextColor(getColor(R.color.minimal_green))
                        send_email_btn.setOnClickListener {
                            swift.romHandler.postInstall(false, SynchronizedArrayList(intent.getStringArrayListExtra("app list")))
                        }
                    result_failed_tab_txt.setTextColor(getColor(R.color.disabled))
                } else {
                    result_failed_tab_txt.setTextColor(getColor(R.color.minimal_red))
                }
                
                if (successList.isNotEmpty()) {
                    result_successful_tab_txt.setTextColor(getColor(R.color.minimal_green))
                } else {
                    send_email_layout.visibility = View.VISIBLE
                    send_email_btn.setOnClickListener {
                        sendErrorLog()
                    }
                    result_successful_tab_txt.setTextColor(getColor(R.color.disabled))
                }

                if(successList.isNotEmpty() && failedList.isNotEmpty()) {
                    fab.show()
                }

                if (!prefs.getBoolean("hide_failed_info", false)) {
                    val failedInfoCard = InfoCard(
                            desc = getString(R.string.info_card_failed_msg),
                            icon = getDrawable(R.drawable.ic_close),
                            btnClick = View.OnClickListener { view ->
                                prefs.edit().putBoolean("hide_failed_info", true).apply()
                                val parent = view.parent as View
                                parent.visibility = View.GONE
                            }
                    ).build(this@SynergySummaryActivity)
                    pagerAdapter.showFailedCard(FAILED_TAB, failedInfoCard)
                }

                if (failed) {
                    container.currentItem = 1
                } else {
                    container.currentItem = 0
                }

                summary_loading_progress.setVisible(false)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val idleHandler = MessageQueue.IdleHandler {
            updateList()
            false
        }
        Looper.myQueue().addIdleHandler(idleHandler)
        if (update) {
            UpdateChecker(this, object : UpdateChecker.Callback() {
                override fun finished(installedCount: Int, hasOption: Boolean, updates: SynchronizedArrayList<String>) {
                }
            }).execute()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun sendErrorLog() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse(getString(R.string.email_support_intent_link))
        emailIntent.putExtra(Intent.EXTRA_EMAIL, Array(1) { getString(R.string.email_reports) })
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_reports_subject))


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
