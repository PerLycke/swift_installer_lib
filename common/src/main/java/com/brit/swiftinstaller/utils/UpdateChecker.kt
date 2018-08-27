package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
import android.os.AsyncTask
import com.brit.swiftinstaller.installer.rom.RomInfo
import java.lang.ref.WeakReference

class UpdateChecker(context: Context, private val callback: Callback) : AsyncTask<Void, Void, UpdateChecker.Output>() {

    private val mConRef: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: Void?): Output {
        var installedCount = 0
        val updates = ArrayList<String>()
        val context = mConRef.get()
        val pm = mConRef.get()!!.packageManager

        clearAppsToUpdate(context!!)
        val overlays = context.assets.list("overlays") ?: emptyArray()
        for (packageName in overlays) {
            if (RomInfo.getRomInfo(context).isOverlayInstalled(packageName)
                    && Utils.isAppInstalled(context, packageName)
                    && pm.getApplicationEnabledSetting(packageName) != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                installedCount++
                if (OverlayUtils.checkOverlayVersion(context, packageName)
                        || Utils.checkAppVersion(context, packageName)) {
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
        abstract fun finished(installedCount: Int, updates: ArrayList<String>)
    }

    inner class Output(var installedCount: Int, var updates: ArrayList<String>)
}