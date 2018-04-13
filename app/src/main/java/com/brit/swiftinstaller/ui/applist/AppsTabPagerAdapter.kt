package com.brit.swiftinstaller.ui.applist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.brit.swiftinstaller.ui.activities.OverlaysActivity

class AppsTabPagerAdapter(fm: FragmentManager, val summary: Boolean,vararg tabs: Int) : FragmentPagerAdapter(fm) {

    private val mApps = HashMap<Int, ArrayList<AppItem>>()

    private var mFragments: ArrayList<AppListFragment> = ArrayList()

    init {
        for(index in tabs) {
            mFragments.add(AppListFragment.instance(summary))
            mApps[index] = ArrayList()
        }
    }

    fun addApp(tab: Int, app: AppItem) {
        mApps[tab]!!.add(app)
        notifyFragmentDataSetChanged(tab)
    }

    fun setApps(tab: Int, apps: ArrayList<AppItem>) {
        mApps[tab]!!.clear()
        mApps[tab]!!.addAll(apps)
        notifyFragmentDataSetChanged(tab)
    }

    fun getAppsCount(tab: Int): Int {
        return mApps[tab]!!.size
    }

    fun clearApps() {
        for (key in mApps.keys) {
            mApps[key]!!.clear()
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

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    fun notifyFragmentDataSetChanged(position: Int) {
        mFragments[position].setAppList(mApps[position])
    }

    override fun getCount(): Int {
        return mFragments.size
    }
}