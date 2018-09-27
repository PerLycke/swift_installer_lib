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

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.installer.Notifier
import com.brit.swiftinstaller.library.utils.*
import kotlinx.android.synthetic.main.progress_dialog_install.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.collections.set

@Suppress("UNUSED_PARAMETER")
class InstallActivity : ThemeActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView
    private val installListener = InstallListener()
    private val handler = Handler()

    private var uninstall = false
    private var update = false

    private var dialog: AlertDialog? = null

    private lateinit var apps: SynchronizedArrayList<String>
    private val updateAppsToUninstall = SynchronizedArrayList<String>()

    private val errorMap: HashMap<String, String> = HashMap()

    fun updateProgress(label: String?, prog: Int, max: Int, uninstall: Boolean) {
        val progress = prog + 1
        if (progressBar.progress < progress) {
            progressBar.isIndeterminate = false
            progressBar.progress = progress
            progressBar.max = max
            progressBar.postInvalidate()
            progressCount.text = getString(R.string.install_count, progress, max)
            progressPercent.text = String.format("%.0f%%", ((progress * 100 / max) + 0.0f))
        }
    }

    private fun installComplete() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(installListener)
        val intent = Intent(this, InstallSummaryActivity::class.java)
        intent.putExtra("update", update)
        errorMap.keys.forEach {
            if (apps.contains(it)) {
                apps.remove(it)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Holder.installApps.clear()
        Holder.installApps.addAll(apps)
        Holder.errorMap.clear()
        Holder.errorMap.putAll(errorMap)
        swift.romInfo.postInstall(false, apps, updateAppsToUninstall, intent)
        finish()
    }

    fun uninstallComplete() {
        val intent = Intent(this@InstallActivity,
                UninstallFinishedActivity::class.java)
        startActivity(intent)
        finish()
    }

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
                show()
            }
        } else {
            installStart()
        }
    }

    private fun installStart() {
        update = intent.getBooleanExtra("update", false)

        if (apps.contains("android")) {
            apps.remove("android")
            apps.add("android")
        }

        val inflate = View.inflate(this, R.layout.progress_dialog_install, null)
        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        val fc = inflate.findViewById<TextView>(R.id.force_close)

        if (uninstall) {
            inflate.install_progress_txt.setText(R.string.progress_uninstalling_title)
            handler.postDelayed({
                if (dialog != null && dialog?.isShowing!!) {
                    fc.visibility = View.VISIBLE
                    fc.setOnClickListener {
                        uninstallComplete()
                    }
                }
            }, 60000)
        }

        builder.setView(inflate)
        dialog = builder.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)

        progressBar = inflate.install_progress_bar
        progressBar.indeterminateTintList = ColorStateList.valueOf(
                swift.romInfo.getCustomizeHandler().getSelection().accentColor)
        progressBar.progressTintList = ColorStateList.valueOf(
                swift.romInfo.getCustomizeHandler().getSelection().accentColor)
        progressCount = inflate.install_progress_count
        progressPercent = inflate.install_progress_percent

        if (!uninstall) {
            updateProgress("", -1, apps.size, uninstall)
        } else {
            progressCount.visibility = View.INVISIBLE
            progressPercent.visibility = View.INVISIBLE
        }

        dialog?.setOnShowListener {
            doAsync {

                val filter = IntentFilter(Notifier.ACTION_FAILED)
                filter.addAction(Notifier.ACTION_INSTALLED)
                filter.addAction(Notifier.ACTION_INSTALL_COMPLETE)
                filter.addAction(Notifier.ACTION_UNINSTALLED)
                filter.addAction(Notifier.ACTION_UNINSTALL_COMPLETE)
                LocalBroadcastManager.getInstance(applicationContext)
                        .registerReceiver(installListener, filter)

                if (uninstall) {
                    if (!ShellUtils.isRootAvailable) {
                        val intentfilter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                        intentfilter.addDataScheme("package")
                        registerReceiver(object : BroadcastReceiver() {
                            var count = apps.size
                            override fun onReceive(context: Context?, intent: Intent?) {
                                count--
                                Log.d("TEST", "total - ${apps.size} : current - $count")
                                if (count == 0) {
                                    uninstallComplete()
                                    context!!.unregisterReceiver(this)
                                }
                            }
                        }, intentfilter)
                        uiThread { _ ->
                            swift.romInfo.postInstall(uninstall = true, apps = apps)
                        }
                    } else {
                        uiThread { _ ->
                            InstallerServiceHelper.uninstall(this@InstallActivity, apps)
                        }
                    }
                } else {
                    uiThread { _ ->
                        InstallerServiceHelper.install(this@InstallActivity, apps)
                    }
                }
            }

        }

        themeDialog()
        dialog?.show()
    }

    override fun recreate() {
        //super.recreate()
    }

    override fun onBackPressed() {
        // do nothing
    }

    override fun onStop() {
        super.onStop()

        if (dialog != null && dialog!!.isShowing) dialog?.cancel()
    }

    inner class InstallListener : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when {
                intent.action == Notifier.ACTION_INSTALLED || intent.action == Notifier.ACTION_UNINSTALLED -> {
                    val pn = intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)
                    val label = packageManager.getApplicationInfo(pn, 0).loadLabel(packageManager)
                    val max = intent.getIntExtra(Notifier.EXTRA_MAX, 0)
                    val progress = intent.getIntExtra(Notifier.EXTRA_PROGRESS, 0)
                    updateProgress(label as String, progress, max, uninstall)
                }
                intent.action == Notifier.ACTION_FAILED -> {
                    errorMap[intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)] =
                            intent.getStringExtra(Notifier.EXTRA_LOG)
                    if (update) {
                        updateAppsToUninstall.add(
                                intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME))
                    }
                }
                intent.action == Notifier.ACTION_INSTALL_COMPLETE -> {
                    installComplete()
                }
                intent.action == Notifier.ACTION_UNINSTALL_COMPLETE -> {
                    uninstallComplete()
                }
            }
        }

    }
}