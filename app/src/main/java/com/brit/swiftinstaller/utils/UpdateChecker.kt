package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.util.Log

class UpdateChecker(val context: Context, val callback: Callback) : AsyncTask<Void, Void, UpdateChecker.Output>() {
    override fun doInBackground(vararg params: Void?): Output {
        var installedCount = 0
        val updates = ArrayList<String>()

        for (packageName in context.assets.list("overlays")) {
            if (Utils.isOverlayInstalled(context, Utils.getOverlayPackageName(packageName))
                    && Utils.isOverlayInstalled(context, packageName)) {
                installedCount++
                Log.d("TEST", "package - $packageName : $installedCount")
                val aInfo = context.packageManager.getApplicationInfo(Utils.getOverlayPackageName(packageName), PackageManager.GET_META_DATA)
                val ver = ShellUtils.inputStreamToString(context.assets.open("overlays/$packageName/version"))
                val version = Integer.parseInt(ver.trim().replace("\"", ""))
                val appVersionCode = context.packageManager.getPackageInfo(packageName, 0).versionCode
                val installedVersionCode = aInfo.metaData.getInt("app_version_code")
                val current = aInfo.metaData.getInt("overlay_version")
                Log.d("TEST", "overlay version - $current : $version + app version - $appVersionCode : $installedVersionCode")
                if (current < version || installedVersionCode < appVersionCode) {
                    updates.add(packageName)
                    addAppToUpdate(context, packageName)
                }
            }
        }
        return Output(installedCount, updates)
    }

    override fun onPostExecute(result: Output?) {
        super.onPostExecute(result)
        callback.finished(result!!.installedCount, result.updates)
    }

    abstract class Callback {
        abstract fun finished(installedCount: Int, updates: ArrayList<String>);
    }

    inner class Output(var installedCount: Int, var updates: ArrayList<String>)
}