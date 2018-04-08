package com.brit.swiftinstaller

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.UpdateChecker

class SwiftInstaller : Application() {

    companion object {
        private lateinit var sSwiftInstaller: SwiftInstaller

        fun getInstance(): SwiftInstaller {
            return sSwiftInstaller
        }
    }

    override fun onCreate() {
        if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_Black);
        }
        super.onCreate()
        sSwiftInstaller = this

        InstallerServiceHelper.connectService(this)
    }

}