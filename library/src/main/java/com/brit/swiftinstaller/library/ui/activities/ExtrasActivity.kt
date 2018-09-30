package com.brit.swiftinstaller.library.ui.activities

import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.applist.AppList
import com.brit.swiftinstaller.library.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.library.utils.OverlayUtils
import com.brit.swiftinstaller.library.utils.setVisible
import com.brit.swiftinstaller.library.utils.swift
import kotlinx.android.synthetic.main.activity_extras.*

class ExtrasActivity : ThemeActivity() {

    companion object {
        private const val EXTRAS_TAB = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extras)
        val extraApps = swift.extrasHandler.appExtras.keys
        val apps = AppList.activeApps

        val pagerAdapter = AppsTabPagerAdapter(supportFragmentManager, false, true, EXTRAS_TAB)
        container.adapter = pagerAdapter

        val handler = MessageQueue.IdleHandler {
            apps.forEach {
                if (extraApps.contains(it.packageName)
                        || OverlayUtils.getOverlayOptions(this, it.packageName).isNotEmpty()) {
                    pagerAdapter.addApp(EXTRAS_TAB, it)
                }
            }
            extras_loading_progress.setVisible(false)
            false
        }
        Looper.myQueue().addIdleHandler(handler)
    }
}
