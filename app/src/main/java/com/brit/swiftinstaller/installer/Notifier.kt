package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.brit.swiftinstaller.BuildConfig


class Notifier(context : Context) {

    companion object {
        private const val installerPackage = BuildConfig.APPLICATION_ID
        const val ACTION_FAILED = installerPackage + ".action.OVERLAY_FAILED"
        const val ACTION_INSTALLED = installerPackage + ".action.OVERLAY_INSTALLED"
        const val ACTION_INSTALL_STARTED = installerPackage + ".action.INSTALL_STARTED"
        const val ACTION_INSTALL_FINISHED = installerPackage + ".action.INSTALL_FINISHED"
        const val EXTRA_PACKAGE_NAME = installerPackage + ".extra.PACKAGE_NAME"
        const val EXTRA_REASON = installerPackage + ".extra.REASON"
        const val EXTRA_PROGRESS = installerPackage + ".extra.PROGRESS"
        const val EXTRA_MAX = installerPackage + ".extra.MAX"
    }

    private val mBroadcaster : LocalBroadcastManager = LocalBroadcastManager.getInstance(context)

    fun broadcastInstallStarted(max: Int) {
        val intent = Intent()
        intent.action = ACTION_INSTALL_STARTED
        intent.putExtra(EXTRA_MAX, max)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        mBroadcaster.sendBroadcast(intent)
    }

    fun broadcastOverlayFailed(packageName: String, reason: Int) {
        val intent = Intent()
        intent.action = ACTION_FAILED
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
        intent.putExtra(EXTRA_REASON, reason)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        mBroadcaster.sendBroadcast(intent)
    }

    fun broadcastOverlayInstalled(packageName: String, progress: Int, max: Int) {
        val intent = Intent()
        intent.action = ACTION_INSTALLED
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
        intent.putExtra(EXTRA_PROGRESS, progress)
        intent.putExtra(EXTRA_MAX, max)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        mBroadcaster.sendBroadcast(intent)
    }
}