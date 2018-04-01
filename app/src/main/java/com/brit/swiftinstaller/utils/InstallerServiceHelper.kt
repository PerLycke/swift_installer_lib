package com.brit.swiftinstaller.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.IInstallerService
import com.brit.swiftinstaller.InstallerService

class InstallerServiceHelper {

    companion object {

        private var sConnection: ServiceConnection? = null
        private var sService: IInstallerService? = null

        private fun getServiceIntent(context: Context): Intent {
            val serviceIntent = Intent(context, InstallerService::class.java)
            serviceIntent.putExtra(InstallerService.ARG_THEME_PACKAGE, BuildConfig.APPLICATION_ID)
            return serviceIntent
        }

        @Suppress("MemberVisibilityCanBePrivate")
        fun startInstallerService(context: Context) {
            context.startService(getServiceIntent(context))
        }

        fun setInstallerCallback(callback: IInstallerCallback) {
            InstallerService.getService().setCallback(callback)
        }

        fun install(apps: List<String>) {
            InstallerService.getService().startInstall(apps)
        }

        fun uninstall(apps: List<String>) {
            InstallerService.getService().startUninstall(apps)
        }

        fun connectService(context: Context) {
            try {
                startInstallerService(context)
                sConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        sService = IInstallerService.Stub.asInterface(service)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                    }
                }
                context.bindService(getServiceIntent(context), sConnection, Context.BIND_AUTO_CREATE)
            } catch (e : Exception) {
            }
        }
    }
}