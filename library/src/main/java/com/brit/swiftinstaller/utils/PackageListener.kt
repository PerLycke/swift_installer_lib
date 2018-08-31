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

package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.OverlayUtils.getOverlayPackageName

class PackageListener : BroadcastReceiver() {

    private val TAG = PackageListener::class.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: ""
        when (intent.action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                if (RomInfo.getRomInfo(context).isOverlayInstalled(getOverlayPackageName(packageName))) {
                    RomInfo.getRomInfo(context).uninstallOverlay(context, packageName)
                }
            }

            Intent.ACTION_PACKAGE_ADDED -> {
                if (OverlayUtils.hasOverlay(context, packageName)) {
                    // show notification that newly installed app can be themed
                }
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                if (RomInfo.getRomInfo(context).isOverlayInstalled(getOverlayPackageName(packageName))) {
                    RomInfo.getRomInfo(context).disableOverlay(packageName)
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "action - ${intent.action}")
            Log.d(TAG, "extra replacing - ${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}")
            Log.d(TAG, "package - ${intent.data?.schemeSpecificPart}")
        }
    }

}