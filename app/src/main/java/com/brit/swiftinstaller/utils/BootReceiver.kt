package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.util.Log
import org.apache.commons.io.FileUtils
import java.io.File

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appsToInstall = getAppsToInstall(context!!)
        Log.d("TEST", "BootReceiver")

        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            InstallerServiceHelper.startInstallerService(context)
        }
    }

}