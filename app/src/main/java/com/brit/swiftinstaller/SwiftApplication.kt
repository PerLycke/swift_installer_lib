package com.brit.swiftinstaller

import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.BusyBox
import com.topjohnwu.superuser.ContainerApp
import com.topjohnwu.superuser.Shell

class SwiftApplication : ContainerApp() {

    override fun onCreate() {
        super.onCreate()

        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        BusyBox.setup(this)
    }
}