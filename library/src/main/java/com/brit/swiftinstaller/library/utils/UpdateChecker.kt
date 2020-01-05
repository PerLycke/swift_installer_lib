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

package com.brit.swiftinstaller.library.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.OverlaysActivity
import com.brit.swiftinstaller.library.ui.applist.AppList
import java.lang.ref.WeakReference

class UpdateChecker(context: Context, private val callback: Callback?) :
        AsyncTask<Void, Int, UpdateChecker.Output>() {

    private val mConRef: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: Void?): Output {
        var installedCount = 0
        var hasOption = false
        val updates = SynchronizedArrayList<String>()
        val context = mConRef.get()

        clearAppsToUpdate(context!!)
        val overlays = context.assets.list("overlays") ?: emptyArray()
        if (Utils.isSynergyCompatibleDevice()) {
            for (packageName in overlays) {
                if (context.pm.isAppInstalled(packageName) && context.pm.isAppEnabled(packageName) && !context.swift.romHandler.getDisabledOverlays().contains(packageName)) {
                    installedCount++
                }
            }
        } else {
            for (packageName in overlays) {
                if (context.swift.romHandler.isOverlayInstalled(packageName)
                        && context.pm.isAppInstalled(packageName)
                        && context.pm.isAppEnabled(packageName)) {
                    installedCount++
                    if (OverlayUtils.checkOverlayVersion(context, packageName)
                            || OverlayUtils.checkAppVersion(context, packageName)) {
                        updates.add(packageName)
                        addAppToUpdate(context, packageName)
                        publishProgress(1)
                    } else {
                        removeAppToUpdate(context, packageName)
                    }
                    AppList.updateApp(context, packageName)
                    if (OverlayUtils.getOverlayOptions(context, packageName).isNotEmpty() ||
                            context.swift.extrasHandler.appExtras.containsKey(packageName)) {
                        hasOption = true
                    }
                }
            }
        }
        return Output(installedCount, hasOption, updates)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if (values[0] == 1) {
            callback?.updateFound()
        }

    }

    override fun onPostExecute(result: Output?) {
        super.onPostExecute(result)
        callback?.finished(result!!.installedCount, result.hasOption, result.updates)
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
        abstract fun finished(installedCount: Int, hasOption: Boolean, updates: SynchronizedArrayList<String>)
        open fun updateFound() {
        }
    }

    inner class Output(var installedCount: Int, var hasOption: Boolean, var updates: SynchronizedArrayList<String>)
}