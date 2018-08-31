package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.BuildConfig

class PackageListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: ""
        when (intent.action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                if (RomInfo.getRomInfo(context).isOverlayInstalled(Utils.getOverlayPackageName(packageName))) {
                    RomInfo.getRomInfo(context).uninstallOverlay(context, packageName)
                }
            }

            Intent.ACTION_PACKAGE_ADDED -> {
                if (OverlayUtils.hasOverlay(context, packageName)) {
                    // show notification that newly installed app can be themed
                }
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                if (RomInfo.getRomInfo(context).isOverlayInstalled(Utils.getOverlayPackageName(packageName))) {
                    RomInfo.getRomInfo(context).disableOverlay(packageName)
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "action - ${intent.action}")
            Log.d("TEST", "extra replacing - ${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}")
            Log.d("TEST", "package - ${intent.data?.schemeSpecificPart}")
        }
    }

}