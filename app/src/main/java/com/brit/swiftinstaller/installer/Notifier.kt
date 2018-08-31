package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.brit.swiftinstaller.library.BuildConfig


class Notifier {

    companion object {
        private const val installerPackage = BuildConfig.APPLICATION_ID
        const val ACTION_FAILED = "$installerPackage.action.OVERLAY_FAILED"
        const val ACTION_INSTALLED = "$installerPackage.action.OVERLAY_INSTALLED"
        const val ACTION_UNINSTALLED = "$installerPackage.action.OVERLAY_UNINSTALLED"
        const val ACTION_INSTALL_COMPLETE = "$installerPackage.action.INSTALL_FINISHED"
        const val ACTION_UNINSTALL_COMPLETE = "$installerPackage.action.UNINSTALL_FINISHED"
        const val EXTRA_PACKAGE_NAME = "$installerPackage.extra.PACKAGE_NAME"
        const val EXTRA_LOG = "$installerPackage.extra.LOG"
        const val EXTRA_PROGRESS = "$installerPackage.extra.PROGRESS"
        const val EXTRA_MAX = "$installerPackage.extra.MAX"


        fun broadcastOverlayFailed(context: Context, packageName: String, errorLog: String) {
            val intent = Intent(ACTION_FAILED)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_LOG, errorLog)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }

        fun broadcastOverlayInstalled(context: Context, packageName: String, progress: Int, max: Int) {
            val intent = Intent(ACTION_INSTALLED)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_PROGRESS, progress)
            intent.putExtra(EXTRA_MAX, max)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }

        fun broadcastOverlayUninstalled(context: Context, packageName: String, progress: Int, max: Int) {
            val intent = Intent(ACTION_UNINSTALLED)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_PROGRESS, progress)
            intent.putExtra(EXTRA_MAX, max)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }

        fun broadcastInstallFinished(context: Context) {
            val intent = Intent(ACTION_INSTALL_COMPLETE)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }

        fun broadcastUninstallFinished(context: Context) {
            val intent = Intent(ACTION_UNINSTALL_COMPLETE)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }
    }
}