package com.brit.swiftinstaller.library.ui.views

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.InstallSummaryActivity
import com.brit.swiftinstaller.library.ui.activities.UninstallFinishedActivity
import com.brit.swiftinstaller.library.utils.Holder.errorMap
import com.brit.swiftinstaller.library.utils.Holder.installApps
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.Utils
import com.brit.swiftinstaller.library.utils.pm
import com.brit.swiftinstaller.library.utils.setVisible
import com.brit.swiftinstaller.library.utils.swift
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.progress_dialog_install.*
import kotlinx.android.synthetic.main.progress_dialog_install.view.*

class InstallerView(val context: Context): LayoutContainer {

    var update = false
    var uninstall = false

    val handler = Handler()

    val updateAppsToUninstall = SynchronizedArrayList<String>()

    override var containerView: ViewGroup =
            View.inflate(context, R.layout.progress_dialog_install, null) as ViewGroup

    init {
        containerView.setBackgroundColor(context.swift.selection.backgroundColor)
    }

    private val wm = context.getSystemService(WindowManager::class.java)

    fun updateProgress(label: String?, icon: Drawable?, prog: Int, max: Int, uninstall: Boolean) {
        var progress = prog
        if (!Utils.isSamsungOreo()) {
            app_icon.setImageDrawable(icon)
            app_name.text = label
        } else {
            progress = prog + 1
        }
        if (install_progress_bar.progress < progress) {
            install_progress_bar.isIndeterminate = false
            install_progress_bar.progress = progress
            install_progress_bar.max = max
            install_progress_bar.postInvalidate()
            install_progress_count.text = context.getString(R.string.install_count, progress, max)
            install_progress_percent.text = String.format("%.0f%%", ((progress * 100 / max) + 0.0f))
        }
    }

    fun installComplete() {
        Log.d("TEST", "installComplete")
        val intent = Intent(context, InstallSummaryActivity::class.java)
        intent.putExtra("update", update)
        errorMap.keys.forEach {
            if (installApps.contains(it)) {
                installApps.remove(it)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.swift.romHandler.postInstall(false, installApps, updateAppsToUninstall, intent)
        handler.postDelayed({
            wm.removeView(containerView)
        }, 600)
    }

    fun uninstallComplete() {
        val intent = Intent(context,
                UninstallFinishedActivity::class.java)
        context.startActivity(intent)
        handler.postDelayed({
            wm.removeView(containerView)
        }, 600)
    }

    fun installStart() {

        if (uninstall) {
            progress_dialog_title.setText(R.string.progress_uninstalling_title)
            handler.postDelayed({
                    force_close.visibility = View.VISIBLE
                    force_close.setOnClickListener {
                        uninstallComplete()
                    }
            }, 120000)
        }

        val params = WindowManager.LayoutParams(context.resources.displayMetrics.widthPixels,
                context.resources.displayMetrics.heightPixels,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT)
        wm.addView(containerView, params)

        if (Utils.isSamsungOreo()) {
            app_icon.setVisible(false)
            app_name.setVisible(false)
        }
        install_progress_bar.indeterminateTintList = ColorStateList.valueOf(
                context.swift.romHandler.getCustomizeHandler().getSelection().accentColor)
        install_progress_bar.progressTintList = ColorStateList.valueOf(
                context.swift.romHandler.getCustomizeHandler().getSelection().accentColor)

        if (!uninstall) {
            if (!Utils.isSamsungOreo()) {
                if (!installApps.isEmpty()) {
                    val ai = context.pm.getApplicationInfo(installApps[0], 0)
                    updateProgress(ai.loadLabel(context.pm) as String, ai.loadIcon(context.pm), 1, installApps.size, uninstall)
                }
            } else {
                updateProgress("", null, -1, installApps.size, uninstall)
            }
        } else {
            install_progress_count.visibility = View.INVISIBLE
            install_progress_percent.visibility = View.INVISIBLE
        }

        /*if (uninstall) {
            if (!ShellUtils.isRootAvailable) {
                val intentfilter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                intentfilter.addDataScheme("package")
                context.registerReceiver(object : BroadcastReceiver() {
                    var count = installApps.size
                    override fun onReceive(context: Context?, intent: Intent?) {
                        count--
                        if (count == 0) {
                            uninstallComplete()
                            context!!.unregisterReceiver(this)
                        }
                    }
                }, intentfilter)
                swift.romHandler.postInstall(uninstall = true, apps = installApps)
            } else {
                InstallerServiceHelper.uninstall(context, installApps)
            }
        } else {
            InstallerServiceHelper.install(context, apps)
        }*/
        //}

        //themeDialog()
        //dialog?.show()
    }

    fun overlayFailed(packageName: String, log: String) {
        errorMap[packageName] = log
        if (update) {
            updateAppsToUninstall.add(packageName)
        }
    }

    fun overlayInstalled(packageName: String, max: Int, progress: Int) {
        val label = context.pm.getApplicationInfo(packageName, 0).loadLabel(context.pm)
        if (!Utils.isSamsungOreo()) {
            val icon = context.pm.getApplicationInfo(packageName, 0).loadIcon(context.pm)
            updateProgress(label as String, icon, progress, max, uninstall)
        } else {
            updateProgress(label as String, null, progress, max, uninstall)
        }
    }
}