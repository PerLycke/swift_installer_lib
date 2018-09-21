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

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.RebootActivity
import com.brit.swiftinstaller.library.utils.OverlayUtils.enableAllOverlays

class EnableOverlaysActivity : Activity() {

    private var notificationManager: NotificationManager? = null

    override fun onResume() {
        super.onResume()

        if (!enableAllOverlays()) {
            return finish()
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("reboot_card", true)
                .apply()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sendNotification()
        finish()
    }

    private fun sendNotification() {

        val notificationID = 101
        val rebootIntent = Intent(this, RebootActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                rebootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                                                     )

        val channelID = "boot_enabled"

        val notification = Notification.Builder(this,
                channelID)
                .setContentTitle(getString(R.string.reboot_notif_title))
                .setStyle(Notification.BigTextStyle()
                        .bigText(getString(R.string.reboot_notif_msg)))
                .setSmallIcon(R.drawable.notif)
                .setChannelId(channelID)
                .setContentIntent(pendingIntent)
                .build()

        notificationManager?.notify(notificationID, notification)
    }
}