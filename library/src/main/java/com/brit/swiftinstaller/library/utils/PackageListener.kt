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

package com.brit.swiftinstaller.library.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.OverlaysActivity
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import org.jetbrains.anko.doAsync

class PackageListener : BroadcastReceiver() {

    private val tag = PackageListener::class.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: ""
        when (intent.action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                if (context.swift.romInfo.isOverlayInstalled(getOverlayPackageName(packageName))) {
                    context.swift.romInfo.uninstallOverlay(context, packageName)
                }
            }

            Intent.ACTION_PACKAGE_ADDED -> {
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (replacing) {
                    if (context.swift.romInfo.isOverlayInstalled(packageName)) {
                        context.swift.romInfo.disableOverlay(packageName)
                        doAsync {
                            UpdateChecker(context, null).execute()
                        }
                    }
                }
                if (!replacing && OverlayUtils.hasOverlay(context,
                                packageName) && newAppNotificationEnabled(context)) {
                    val notificationID = 102
                    val rebootIntent = Intent(context, OverlaysActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                            context.applicationContext,
                            0,
                            rebootIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)

                    val channelID = "app_listener"

                    val notification = Notification.Builder(context.applicationContext,
                            channelID)
                            .setContentTitle(context.getString(R.string.notif_new_app_title))
                            .setContentText(context.getString(R.string.notif_new_app_summary,
                                    context.packageManager.getApplicationInfo(packageName,
                                            0).loadLabel(context.packageManager)))
                            .setSmallIcon(R.drawable.notif)
                            .setChannelId(channelID)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()
                    val notifManager = context.applicationContext.getSystemService(
                            NotificationManager::class.java)
                    notifManager.notify(notificationID, notification)
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(tag, "action - ${intent.action}")
            Log.d(tag, "extra replacing - ${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}")
            Log.d(tag, "package - ${intent.data?.schemeSpecificPart}")
        }
    }

}
