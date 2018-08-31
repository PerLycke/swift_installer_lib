package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.brit.swiftinstaller.BuildConfig


class Notifier {

    companion object {
        private const val installerPackage = BuildConfig.APPLICATION_ID
        const val ACTION_FAILED = "$installerPackage.action.OVERLAY_FAILED"
        const val ACTION_INSTALLED = "$installerPackage.action.OVERLAY_INSTALLED"
        const val EXTRA_PACKAGE_NAME = "$installerPackage.extra.PACKAGE_NAME"
        const val EXTRA_LOG = "$installerPackage.extra.LOG"
        const val EXTRA_PROGRESS = "$installerPackage.extra.PROGRESS"
        const val EXTRA_MAX = "$installerPackage.extra.MAX"


        fun broadcastOverlayFailed(context: Context, packageName: String, errorLog: String) {
            val intent = Intent(ACTION_FAILED)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_LOG, errorLog)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun broadcastOverlayInstalled(context: Context, packageName: String, progress: Int, max: Int) {
            Log.d("TEST", "broadcastOverlayInstalled")
            val intent = Intent(ACTION_INSTALLED)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_PROGRESS, progress)
            intent.putExtra(EXTRA_MAX, max)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }
    }
}