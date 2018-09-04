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

package com.brit.swiftinstaller.library

import android.app.job.JobParameters
import android.app.job.JobService
import com.brit.swiftinstaller.library.installer.OverlayManager
import com.brit.swiftinstaller.library.utils.InstallerServiceHelper

class InstallerService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return om != null && om!!.isRunning()
    }

    override fun onStartJob(params: JobParameters): Boolean {
        om = OverlayManager(this)
        om!!.setCallback(object : OverlayManager.Callback {
            override fun installFinished() {
                jobFinished(params, false)
            }
        })
        val apps = params.extras.getStringArray(InstallerServiceHelper.EXTRAS_APPS) ?: emptyArray()
        if (params.jobId == InstallerServiceHelper.INSTALL_JOB) {
            om!!.installOverlays(apps)
        } else if (params.jobId == InstallerServiceHelper.UNINSTALL_JOB) {
            om!!.uninstallOverlays(apps)
        }
        return true
    }

    private var om: OverlayManager? = null
}
