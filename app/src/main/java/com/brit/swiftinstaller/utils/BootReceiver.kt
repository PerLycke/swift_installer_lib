package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appsToInstall = getAppsToInstall(context!!)

        if (intent!!.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            for (packageName in appsToInstall) {
                runCommand("cmd overlay enable " + packageName)
                Log.d("TEST", "enable - " + packageName)
            }
        }
    }

}