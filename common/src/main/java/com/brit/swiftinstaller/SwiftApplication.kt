package com.brit.swiftinstaller

import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.BusyBox
import com.topjohnwu.superuser.ContainerApp
import com.topjohnwu.superuser.Shell

class SwiftApplication : ContainerApp() {

    val installApps = arrayListOf<String>()
    val errorMap = HashMap<String, String>()

    override fun onCreate() {
        super.onCreate()

        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        BusyBox.setup(this)
    }
}