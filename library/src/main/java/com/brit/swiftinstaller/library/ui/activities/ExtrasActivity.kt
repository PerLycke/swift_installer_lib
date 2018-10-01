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
