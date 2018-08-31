package com.brit.swiftinstaller.utils

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.brit.swiftinstaller.InstallerService

class InstallerServiceHelper {

    companion object {

        const val INSTALL_JOB = 101
        const val UNINSTALL_JOB = 102

        const val EXTRAS_APPS = "com.brit.swiftinstaller.APPS"

        private fun getServiceComponent(context: Context): ComponentName {
            return ComponentName(context, InstallerService::class.java)
        }

        fun install(context: Context, apps: List<String>) {
            val extras = PersistableBundle()
            extras.putStringArray(EXTRAS_APPS, apps.toTypedArray())
            val params = JobInfo.Builder(INSTALL_JOB, getServiceComponent(context))
                    .setExtras(extras)
                    .setRequiresStorageNotLow(true)
                    .build()
            context.getSystemService(JobScheduler::class.java).schedule(params)
        }

        fun uninstall(context: Context, apps: List<String>) {
            val extras = PersistableBundle()
            extras.putStringArray(EXTRAS_APPS, apps.toTypedArray())
            val params = JobInfo.Builder(UNINSTALL_JOB, getServiceComponent(context))
                    .setExtras(extras)
                    .setRequiresStorageNotLow(true)
                    .build()
            context.getSystemService(JobScheduler::class.java).schedule(params)
        }
    }
}