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

package com.brit.swiftinstaller.library.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.Holder.installApps

class InstallActivity : ThemeActivity() {

    private var uninstall = false
    private var update = false

    private lateinit var apps: SynchronizedArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uninstall = intent.getBooleanExtra("uninstall", false)
        apps = SynchronizedArrayList(intent.getStringArrayListExtra("apps"))

        if (!uninstall && apps.contains("android") && !prefs.getBoolean("android_install_dialog", false)) {
            prefs.edit().putBoolean("android_install_dialog", true).apply()
            alert {
                title = getString(R.string.installing_and_uninstalling_title)
                message = getString(R.string.installing_and_uninstalling_msg)
                positiveButton(R.string.proceed) { dialog ->
                    dialog.dismiss()
                    installStart()
                }
                isCancelable = false
                show()
            }
        } else {
            installStart()
        }
    }

    private fun installStart() {

        if (apps.contains("android")) {
            apps.remove("android")
            apps.add("android")
        }
        installApps.clear()
        installApps.addAll(apps)

        update = intent.getBooleanExtra("update", false)
        if (uninstall) {
            if (!ShellUtils.isRootAvailable) {
                val intentfilter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                intentfilter.addDataScheme("package")
                registerReceiver(object : BroadcastReceiver() {
                    var count = apps.size
                    override fun onReceive(context: Context?, intent: Intent?) {
                        count--
                        if (count == 0) {
                            //uninstallComplete()
                            context!!.unregisterReceiver(this)
                        }
                    }
                }, intentfilter)
                swift.romHandler.postInstall(uninstall = true, apps = apps)
            } else {
                InstallerServiceHelper.uninstall(this, apps)
            }
        } else {
            InstallerServiceHelper.install(this, apps)
        }
    }

    override fun recreate() {
        //super.recreate()
    }

    override fun onBackPressed() {
        Log.d("TEST", "onBackPressed")
        // do nothing
    }
}