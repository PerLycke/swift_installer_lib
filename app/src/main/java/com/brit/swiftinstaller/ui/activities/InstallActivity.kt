package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.R.id.palette
import com.brit.swiftinstaller.installer.Notifier
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.utils.getBackgroundColor
import kotlinx.android.synthetic.main.progress_dialog_install.view.*
import java.util.ArrayList


@Suppress("UNUSED_PARAMETER")
class InstallActivity : ThemeActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView
    private val installListener = InstallListener()

    private var uninstall = false
    private var update = false

    private lateinit var dialog: AlertDialog

    private lateinit var apps: ArrayList<String>
    private val updateAppsToUninstall = ArrayList<String>()

    private val errorMap: HashMap<String, String> = HashMap()

    fun updateProgress(label: String?, prog: Int, max: Int, uninstall: Boolean) {
        val progress = prog + 1
        if (progressBar.progress < progress) {
            progressBar.progress = progress
            progressBar.max = max
            progressBar.postInvalidate()
            progressCount.text = getString(R.string.install_count, progress, max)
            progressPercent.text = String.format("%.0f%%", ((progress * 100 / max) + 0.0f))
        }
    }

    private fun installComplete(uninstall: Boolean) {
        if (!uninstall) {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(installListener)
            val intent = Intent(this, InstallSummaryActivity::class.java)
            intent.putExtra("errorMap", Utils.mapToBundle(errorMap))
            errorMap.keys.forEach {
                if (apps.contains(it)) {
                    apps.remove(it)
                }
            }
            intent.putExtra("apps", apps)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
            RomInfo.getRomInfo(this).postInstall(uninstall, apps, updateAppsToUninstall, intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uninstall = intent.getBooleanExtra("uninstall", false)
        update = intent.getBooleanExtra("update", false)
        apps = intent.getStringArrayListExtra("apps")

        val inflate = View.inflate(this, R.layout.progress_dialog_install, null)
        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)

        themeDialog()

        builder.setView(inflate)
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        if (uninstall) {
            inflate.install_progress_txt.setText(R.string.progress_uninstalling_title)
        }

        val filter = IntentFilter(Notifier.ACTION_FAILED)
        filter.addAction(Notifier.ACTION_INSTALLED)
        filter.addAction(Notifier.ACTION_INSTALL_COMPLETE)
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(installListener, filter)

        progressBar = inflate.install_progress_bar
        progressBar.isIndeterminate = uninstall
        progressCount = inflate.install_progress_count
        progressPercent = inflate.install_progress_percent

        if (!uninstall) {
            updateProgress("", -1, apps.size, uninstall)
        } else {
            progressCount.visibility = View.INVISIBLE
            progressPercent.visibility = View.INVISIBLE
        }
        dialog.show()

        if (uninstall && !ShellUtils.isRootAvailable) {
            val intentfilter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            intentfilter.addDataScheme("package")
            registerReceiver(object : BroadcastReceiver() {
                var count = apps.size
                override fun onReceive(context: Context?, intent: Intent?) {
                    count--
                    if (count == 0) {
                        startActivity(Intent(this@InstallActivity,
                                UninstallFinishedActivity::class.java))
                        finish()
                        context!!.unregisterReceiver(this)
                    }
                }
            }, intentfilter)
            RomInfo.getRomInfo(this).postInstall(true, apps, null, null)
        }
        if (!uninstall) {
            InstallerServiceHelper.install(this, apps)
        }
    }

    override fun recreate() {
        //super.recreate()
    }

    override fun onBackPressed() {
        // do nothing
    }

    override fun finish() {
        super.finish()
        if (dialog.isShowing) dialog.cancel()
    }

    inner class InstallListener : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Notifier.ACTION_INSTALLED) {
                val pn = intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)
                val label = packageManager.getApplicationInfo(pn, 0).loadLabel(packageManager)
                val max = intent.getIntExtra(Notifier.EXTRA_MAX, 0)
                val progress = intent.getIntExtra(Notifier.EXTRA_PROGRESS, 0)
                updateProgress(label as String, progress, max, uninstall)
            } else if (intent.action == Notifier.ACTION_FAILED) {
                errorMap[intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)] =
                        intent.getStringExtra(Notifier.EXTRA_LOG)
                if (update) {
                    updateAppsToUninstall.add(intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME))
                }
            } else if (intent.action == Notifier.ACTION_INSTALL_COMPLETE) {
                installComplete(uninstall)
            }
        }

    }
}