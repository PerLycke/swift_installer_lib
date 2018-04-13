package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TEST", "action - ${intent!!.action}")
        Log.d("TEST", "extra replacing - ${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}")
        Log.d("TEST", "package - ${intent.data.schemeSpecificPart}")
    }

}