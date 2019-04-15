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

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.applist.AppItem
import com.brit.swiftinstaller.library.ui.applist.AppList
import com.brit.swiftinstaller.library.ui.applist.AppListFragment
import com.brit.swiftinstaller.library.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.checkVersionCompatible
import com.brit.swiftinstaller.library.utils.OverlayUtils.getApkLink
import com.brit.swiftinstaller.library.utils.OverlayUtils.getAvailableOverlayVersions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_overlays.*
import kotlinx.android.synthetic.main.sheet_confirm_uninstall.view.*
import kotlinx.android.synthetic.main.tab_layout_overlay.*
import kotlinx.android.synthetic.main.tab_overlays_updates.*
import kotlinx.android.synthetic.main.toolbar_overlays.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class OverlaysActivity : ThemeActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        const val UPDATE_TAB = 2
    }
    private lateinit var pagerAdapter: AppsTabPagerAdapter
    private var checked = 0
    private var apps = 0
    private val handler = Handler()

    private val subscription = { index: Int ->
        when(index) {
            AppList.ACTIVE -> pagerAdapter.setApps(ACTIVE_TAB, AppList.activeApps)
            AppList.INACTIVE -> pagerAdapter.setApps(INSTALL_TAB, AppList.inactiveApps)
            AppList.UPDATE -> pagerAdapter.setApps(UPDATE_TAB, AppList.appUpdates)
        }
    }

    override fun onPause() {
        super.onPause()
        AppList.removeSubscriber(subscription)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlays)

        pagerAdapter = AppsTabPagerAdapter(supportFragmentManager,
                false, false, INSTALL_TAB, ACTIVE_TAB, UPDATE_TAB)
        pagerAdapter.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                val packageInfo =  try {
                    packageManager.getPackageInfo(appItem.packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    return
                }
                val supported = if (checkVersionCompatible(this@OverlaysActivity, appItem.packageName)) {
                    "<font color='#47AE84'><b> - (supported!)</b></font>"
                } else {
                    "<font color='#FF6868'><b> - (not supported)</b></font>"
                }
                alert {
                    title = appItem.title
                    icon = appItem.icon
                        val m = Html.fromHtml(
                                "<br><b>Installed app version: " +
                                        "</b><br><br>" +
                                        packageInfo.versionName + supported +
                                        "<br><br><b>" +
                                        "Supported versions: " +
                                        "</b><br><br>" +
                                        getAvailableOverlayVersions(
                                                this@OverlaysActivity, packageInfo, appItem.packageName), Html.FROM_HTML_MODE_LEGACY)
                        val ss = getApkLink(this@OverlaysActivity, m, packageInfo, appItem.packageName)
                        message = ss
                    positiveButton(R.string.ok) { dialog ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
        })
        pagerAdapter.setAppCheckBoxClickListener(object :
                AppListFragment.AppCheckBoxClickListener {
            override fun onCheckBoxClick(appItem: AppItem) {
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
                handler.post {
                    checked = pagerAdapter.getCheckedCount(container.currentItem)
                }
            }
        })
        pagerAdapter.setViewClickListener(object : AppListFragment.ViewClickListener {
            override fun onClick(appItem: AppItem) {
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
                handler.post {
                    checked = pagerAdapter.getCheckedCount(container.currentItem)
                }
            }
        })
        pagerAdapter.setRequiredApps(INSTALL_TAB,
                swift.romHandler.getRequiredApps())

        search_view.setOnSearchClickListener {
            toolbar_overlays_main_content.visibility = View.GONE
            select_all_btn.isClickable = false
            select_all_btn.isEnabled = false
        }
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pagerAdapter.querySearch(container.currentItem, newText!!)
                return true
            }
        })
        search_view.setOnCloseListener {
            onClose()
            false
        }
        val textViewId =
                search_view.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        setCursorPointerColor(textViewId, swift.selection.accentColor)
        setCursorDrawableColor(textViewId, swift.selection.accentColor)

        container.offscreenPageLimit = 2

        select_all_btn.setOnClickListener {
            if (checked == apps) {
                pagerAdapter.selectAll(container.currentItem, false)
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
            } else {
                pagerAdapter.selectAll(container.currentItem, true)
            }
            pagerAdapter.notifyFragmentDataSetChanged(container.currentItem)
            handler.post {
                checked = pagerAdapter.getCheckedCount(container.currentItem)
            }
        }

        container.adapter = pagerAdapter
        container.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(tabs_overlays_root))
        tabs_overlays_root.addOnTabSelectedListener(
                TabLayout.ViewPagerOnTabSelectedListener(container))
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                doAsync {
                    apps = pagerAdapter.getCheckableCount(this@OverlaysActivity,
                            container.currentItem)
                }
                val checked = pagerAdapter.getCheckedCount(position)
                select_all_btn.isChecked =
                        checked == pagerAdapter.getAppsCount(position) && checked > 0
                setBackgroundImage()
            }

        })

        if (intent.hasExtra("tab")) {
            container.currentItem = intent.getIntExtra("tab", 0)
        }
    }

    private fun setBackgroundImage() {

        if (pagerAdapter.getAppsCount(container.currentItem) == 0) {
            when (container.currentItem) {
                0 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_inactive))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_inactive)
                    empty_list_text.alpha = 0.2f
                }
                1 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_active))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_active)
                    empty_list_text.alpha = 0.2f
                }
                2 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_updates))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_updates)
                    empty_list_text.alpha = 0.2f
                }
            }
        } else {
            empty_list_image.setImageDrawable(null)
            empty_list_text.text = ""
        }
    }

    private fun onClose() {
        toolbar_overlays_main_content.visibility = View.VISIBLE
        select_all_btn.isClickable = true
        select_all_btn.isEnabled = true
    }

    override fun onBackPressed() {
        if (!search_view.isIconified) {
            search_view.onActionViewCollapsed()
            onClose()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        AppList.addSubscriber(subscription)
        toolbar_subtitle_current_accent.setTextColor(swift.selection.accentColor)
        toolbar_subtitle_current_accent.text = getString(R.string.hex_string,
                String.format("%06x", swift.selection.accentColor).substring(2))
        if (select_all_btn.isChecked) {
            select_all_btn.isChecked = false
        }
        handler.post {
            updateAdapter()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun updateAdapter() {
        select_all_btn.visibility = View.INVISIBLE
        select_all_btn.isClickable = false
        loading_progress.visibility = View.VISIBLE
        loading_progress.indeterminateDrawable.setColorFilter(swift.selection.accentColor,
                PorterDuff.Mode.SRC_ATOP)

        handler.post {
            pagerAdapter.setApps(INSTALL_TAB, AppList.inactiveApps)
            pagerAdapter.setApps(ACTIVE_TAB, AppList.activeApps)
            pagerAdapter.setApps(UPDATE_TAB, AppList.appUpdates)

            handler.post {
                checked = pagerAdapter.getCheckedCount(container.currentItem)
                apps = pagerAdapter.getCheckableCount(this@OverlaysActivity,
                        container.currentItem)

                handler.post {
                    setBackgroundImage()
                    loading_progress.visibility = View.INVISIBLE
                    select_all_btn.visibility = View.VISIBLE
                    select_all_btn.isClickable = true

                    if (!AppList.appUpdates.isEmpty()) {
                        update_tab_indicator.visibility = View.VISIBLE
                    } else if (update_tab_indicator.visibility == View.VISIBLE) {
                        update_tab_indicator.visibility = View.GONE
                    }
                    pagerAdapter.notifyFragmentDataSetChanged(container.currentItem)
                }
            }
        }
    }

    fun customizeBtnClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, CustomizeActivity::class.java)
        startActivity(intent)
    }

    private fun getCheckedItems(index: Int): SynchronizedArrayList<AppItem> {
        return pagerAdapter.getCheckedItems(index)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun fabClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_overlays_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(swift.selection.backgroundColor)
        bottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.install)
        val uninstall = sheetView.findViewById<View>(R.id.uninstall)
        val update = sheetView.findViewById<View>(R.id.update)

        val sheetListener = View.OnClickListener {
            when (it) {
                install -> {
                    bottomSheetDialog.dismiss()
                    installAction()
                }
                uninstall -> {
                    bottomSheetDialog.dismiss()
                    uninstallAction()
                }
                update -> {
                    bottomSheetDialog.dismiss()
                    updateAction()
                }
            }
        }

        install.setOnClickListener(sheetListener)
        uninstall.setOnClickListener(sheetListener)
        update.setOnClickListener(sheetListener)

        when {
            container.currentItem == INSTALL_TAB -> {
                uninstall.visibility = View.GONE
                update.visibility = View.GONE
            }
            container.currentItem == ACTIVE_TAB -> {
                install.visibility = View.GONE
                val checked = getCheckedItems(ACTIVE_TAB)
                val updates = getAppsToUpdate(this)
                var updatesAvailable = false
                checked.forEach {
                    if (!updatesAvailable)
                        updatesAvailable = updates.contains(it.packageName)
                }
                update.visibility = if (updatesAvailable || BuildConfig.DEBUG) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            container.currentItem == UPDATE_TAB -> {
                install.visibility = View.GONE
            }
        }
    }

    private fun installAction() {
        val checked = getCheckedItems(container.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        val intent = Intent(this, InstallActivity::class.java)
        val apps = SynchronizedArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        pagerAdapter.clearCheckedItems()
        startActivity(intent)
    }

    private fun uninstallAction() {
        val checked = getCheckedItems(container.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_confirm_uninstall, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(swift.selection.backgroundColor)
        bottomSheetDialog.show()

        sheetView.confirm_layout.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallProgressAction(checked)

            longToast("This can take a lot of time, have patience!")
        }

        sheetView.cancel_layout.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun uninstallProgressAction(checked: SynchronizedArrayList<AppItem>) {
        val intent = Intent(this, InstallActivity::class.java)
        val apps = SynchronizedArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("uninstall", true)
        pagerAdapter.clearCheckedItems()
        startActivity(intent)
    }

    private fun updateAction() {
        val checked = getCheckedItems(container.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, hasOption: Boolean, updates: SynchronizedArrayList<String>) {
            }
        }).execute()
        val intent = Intent(this, InstallActivity::class.java)
        val apps = SynchronizedArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("update", true)
        pagerAdapter.clearCheckedItems()
        startActivity(intent)
    }

    fun overlaysBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressed()
    }

    @Suppress("UNUSED_PARAMETER")
    fun blockedPackagesInfo(view: View) {
        alert {
            title = getString(R.string.blocked_packages_title)
            message = getString(R.string.blocked_packages_message)
            positiveButton(R.string.ok) { dialog ->
                dialog.dismiss()
            }
            show()
        }
    }
}
