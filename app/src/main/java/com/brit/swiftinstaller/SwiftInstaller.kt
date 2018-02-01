package com.brit.swiftinstaller

import android.app.Application
import com.brit.swiftinstaller.utils.InstallerServiceHelper

class SwiftInstaller : Application() {

    companion object {
        private lateinit var sSwiftInstaller: SwiftInstaller

        fun getInstance(): SwiftInstaller {
            return sSwiftInstaller
        }
    }

    override fun onCreate() {
        super.onCreate()
        sSwiftInstaller = this

        InstallerServiceHelper.connectService(this)
    }

}