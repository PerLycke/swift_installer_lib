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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
import android.os.AsyncTask
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.OverlaysActivity
import com.brit.swiftinstaller.library.ui.applist.AppList
import java.lang.ref.WeakReference

class UpdateChecker(context: Context, private val callback: Callback?) :
        AsyncTask<Void, Void, UpdateChecker.Output>() {

    private val mConRef: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: Void?): Output {
        var installedCount = 0
        val updates = ArrayList<String>()
        val context = mConRef.get()
        val pm = mConRef.get()!!.packageManager

        clearAppsToUpdate(context!!)
        val overlays = context.assets.list("overlays") ?: emptyArray()
        for (packageName in overlays) {
            if (context.swift.romInfo.isOverlayInstalled(packageName)
                    && Utils.isAppInstalled(context, packageName)
                    && pm.getApplicationEnabledSetting(
                            packageName) != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                installedCount++
                if (OverlayUtils.checkOverlayVersion(context, packageName)
                        || OverlayUtils.checkAppVersion(context, packageName)) {
                    updates.add(packageName)
                    addAppToUpdate(context, packageName)
                }
            }
        }
        return Output(installedCount, updates)
    }

    override fun onPostExecute(result: Output?) {
        super.onPostExecute(result)
        callback?.finished(result!!.installedCount, result.updates)
        result?.let {
            mConRef.get()?.let { ctx ->
                it.updates.forEach { packageName ->
                    AppList.addApp(ctx, packageName)
                }
            }
        }
        if (result!!.updates.isNotEmpty() && callback == null) {
            postNotification()
        }
    }

    private fun postNotification() {
        val context = mConRef.get() ?: return
        if (!updateNotificationEnabled(context)) return
        val notificationID = 103
        val rebootIntent = Intent(context, OverlaysActivity::class.java)
        rebootIntent.putExtra("tab", OverlaysActivity.UPDATE_TAB)
        val pendingIntent = PendingIntent.getActivity(
                context.applicationContext,
                0,
                rebootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val channelID = "app_listener"

        val notification = Notification.Builder(context,
                channelID)
                .setContentTitle(context.getString(R.string.notif_update_title))
                .setContentText(context.getString(R.string.notif_update_summary))
                .setSmallIcon(R.drawable.notif)
                .setChannelId(channelID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        val notifManager = context.getSystemService(NotificationManager::class.java)
        notifManager.notify(notificationID, notification)
    }

    abstract class Callback {
        abstract fun finished(installedCount: Int, updates: ArrayList<String>)
    }

    inner class Output(var installedCount: Int, var updates: ArrayList<String>)
}