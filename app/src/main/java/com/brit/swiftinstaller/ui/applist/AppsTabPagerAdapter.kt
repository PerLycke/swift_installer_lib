package com.brit.swiftinstaller.ui.applist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.brit.swiftinstaller.ui.activities.InstallSummaryActivity

class AppsTabPagerAdapter(fm: androidx.fragment.app.FragmentManager, val summary: Boolean, vararg tabs: Int) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    private val mApps = HashMap<Int, ArrayList<AppItem>>()

    private var mFragments: ArrayList<AppListFragment> = ArrayList()

    private val requiredApps = HashMap<Int, Array<String>>()

    init {
        for (index in tabs) {
            mFragments.add(AppListFragment.instance(summary,
                    (index == InstallSummaryActivity.FAILED_TAB)))
            mApps[index] = ArrayList()
        }
    }

    fun setAlertIconClickListener(listener: AppListFragment.AlertIconClickListener) {
        mFragments.forEach {
            it.alertIconClickListener = listener
        }
    }

    fun querySearch(tab: Int, query: String) {
        mFragments[tab].querySearch(query)
    }

    fun addApp(tab: Int, app: AppItem) {
        mApps[tab]!!.add(app)
        notifyFragmentDataSetChanged(tab)
    }

    fun setRequiredApps(tab: Int, apps: Array<String>) {
        requiredApps[tab] = apps
        notifyFragmentDataSetChanged(tab)
    }

    fun getAppsCount(tab: Int): Int {
        return mApps[tab]!!.size
    }

    fun clearApps() {
        for (key in mApps.keys) {
            mApps[key]!!.clear()
            mFragments[key].selectAll(false)
            notifyFragmentDataSetChanged(key)
        }
    }

    fun selectAll(index: Int, checked: Boolean) {
        mFragments[index].selectAll(checked)
    }

    fun getCheckedCount(index: Int): Int {
        return mFragments[index].getCheckedItems().count()
    }

    fun getApps(tab: Int): ArrayList<AppItem> {
        return mApps[tab]!!
    }

    fun getCheckedItems(index: Int): ArrayList<AppItem> {
        return mFragments[index].getCheckedItems()
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return mFragments[position]
    }

    fun notifyFragmentDataSetChanged(position: Int) {
        mFragments[position].setAppList(mApps[position])
        if (requiredApps[position] != null) {
            mFragments[position].setRequiredAppList(requiredApps[position]!!)
        }
    }

    override fun getCount(): Int {
        return mFragments.size
    }
}