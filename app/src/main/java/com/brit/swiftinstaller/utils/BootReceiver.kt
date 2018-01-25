package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appsToInstall = getAppsToInstall(context!!)

        for (packageName in appsToInstall) {
            runCommand("cmd overlay enable " + packageName)
        }
    }

}