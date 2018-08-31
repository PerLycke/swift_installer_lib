/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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