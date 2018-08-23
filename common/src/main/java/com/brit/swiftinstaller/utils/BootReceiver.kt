package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.brit.swiftinstaller.library.BuildConfig

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val enable = Intent(context, EnableOverlaysActivity::class.java)
            enable.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.applicationContext.startActivity(enable)
            if (BuildConfig.DEBUG) {
                val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
                filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
                filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
                filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                filter.addDataScheme("package")
                context.applicationContext.registerReceiver(PackageListener(), filter)
            }
        }
    }
}