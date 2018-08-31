package com.brit.swiftinstaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.BuildConfig

class PackageListener : BroadcastReceiver() {

    private val TAG = PackageListener::class.simpleName

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
                if (OverlayUtils.isSwiftOverlay(packageName)) {
                    try {
                        val oInfo = context.packageManager.getPackageInfo(packageName, 0)
                        if (oInfo.getVersionCode() > getAppVersion(context, packageName)) {
                            setAppVersion(context, packageName, oInfo.getVersionCode())
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // ignore
                    }
                }
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                if (RomInfo.getRomInfo(context).isOverlayInstalled(Utils.getOverlayPackageName(packageName))) {
                    RomInfo.getRomInfo(context).disableOverlay(packageName)
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "action - ${intent.action}")
            Log.d(TAG, "extra replacing - ${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}")
            Log.d(TAG, "package - ${intent.data?.schemeSpecificPart}")
        }
    }

}