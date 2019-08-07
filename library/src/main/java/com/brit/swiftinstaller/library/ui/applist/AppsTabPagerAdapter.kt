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

package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.brit.swiftinstaller.library.ui.activities.InstallSummaryActivity
import com.brit.swiftinstaller.library.utils.OverlayUtils.checkVersionCompatible
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.swift

class AppsTabPagerAdapter(fm: FragmentManager, summary: Boolean, extras: Boolean, vararg tabs: Int) :
        FragmentStatePagerAdapter(fm) {

    private var fragments: SynchronizedArrayList<AppListFragment> = SynchronizedArrayList()
    private val handler = Handler()

    init {
        for (index in tabs) {
            fragments.add(AppListFragment.instance(summary, extras,
                    (index == InstallSummaryActivity.FAILED_TAB)))
        }
    }

    fun retainInstance() {
        fragments.forEach {
            handler.post {
                it.retainInstance = true
            }
        }
    }

    fun showFailedCard(tab: Int, card: View) {
        handler.post {
            fragments[tab].addCard(card)
        }
    }

    fun setAlertIconClickListener(listener: AppListFragment.AlertIconClickListener) {
        fragments.forEach {
            it.alertIconClickListener = listener
        }
    }

    fun setAppCheckBoxClickListener(listener: AppListFragment.AppCheckBoxClickListener) {
        fragments.forEach {
            it.appCheckBoxClickListener = listener
        }
    }

    fun addApp(tab: Int, app: AppItem) {
        handler.post {
            fragments[tab].addApp(app)
        }
    }

    fun setViewClickListener(listener: AppListFragment.ViewClickListener) {
        fragments.forEach {
            it.viewClickListener = listener
        }
    }

    fun querySearch(tab: Int, query: String) {
        handler.post { fragments[tab].querySearch(query) }
    }

    fun setRequiredApps(tab: Int, apps: Array<String>) {
        fragments[tab].setRequiredAppList(apps)
    }

    fun getAppsCount(tab: Int): Int {
        return fragments[tab].apps.size
    }

    fun getCheckableCount(context: Context, tab: Int): Int {
        val checkableList = arrayListOf<AppItem>()
        for (item in fragments[tab].apps) {
            if (checkVersionCompatible(context, item.packageName) ||
                    context.swift.romHandler.isOverlayInstalled(item.packageName)) {
                checkableList.add(item)
            }
        }
        return checkableList.size
    }

    fun clearApps() {
        for (key in fragments.indices) {
            fragments[key].apps.clear()
            fragments[key].selectAll(false)
            notifyFragmentDataSetChanged(key)
        }
    }

    fun selectAll(index: Int, checked: Boolean) {
        fragments[index].selectAll(checked)
    }

    fun getCheckedCount(index: Int): Int {
        return fragments[index].getCheckedItems().count()
    }

    fun setApps(tab: Int, apps: SynchronizedArrayList<AppItem>) {
        handler.post {
            fragments[tab].setAppList(apps)
        }
    }

    fun getApps(tab: Int): SynchronizedArrayList<AppItem> {
        return fragments[tab].apps
    }

    fun getCheckedItems(index: Int): SynchronizedArrayList<AppItem> {
        return fragments[index].getCheckedItems()
    }

    fun clearCheckedItems() {
        fragments.forEach {
            it.clearCheckedITems()
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    fun notifyFragmentDataSetChanged(position: Int) {
        handler.post {
            fragments[position].notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return fragments.size
    }
}