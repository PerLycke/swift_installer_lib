package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.installer.Notifier
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.addAppToUninstall
import com.brit.swiftinstaller.utils.rom.RomInfo
import kotlinx.android.synthetic.main.progress_dialog_install.view.*
import java.util.ArrayList


@Suppress("UNUSED_PARAMETER")
class InstallActivity : ThemeActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView
    private val installListener = InstallListener()

    private var uninstall = false

    private lateinit var apps: ArrayList<String>

    private val errorMap: HashMap<String, String> = HashMap()

    fun updateProgress(label: String?, prog: Int, maximum: Int, uninstall: Boolean) {
        val max = maximum + 1
        val progress = prog + 1
        if (progressBar.progress < progress) {
            progressBar.progress = progress
            progressBar.max = max
            progressBar.postInvalidate()
            progressCount.text = getString(R.string.install_count, progress, max)
            progressPercent.text = String.format("%.0f%%", ((progress * 100 / max) + 0.0f))
        }
        Log.d("TEST", "progress - $progress/$max")
        if (progress == max) {
            installComplete(uninstall)
        }
    }

    private fun installComplete(uninstall: Boolean) {
        if (!uninstall) {
            val intent = Intent(this, InstallSummaryActivity::class.java)
            intent.putExtra("errorMap", Utils.mapToBundle(errorMap))
            errorMap.keys.forEach { if (apps.contains(it)) { apps.remove(it)}}
            intent.putExtra("apps", apps)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            RomInfo.getRomInfo(this).postInstall(uninstall, apps, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uninstall = intent.extras.getBoolean("uninstall", false)
        apps = intent.getStringArrayListExtra("apps")
        apps.forEach { Log.d("TEST", "install $it") }

        val inflate = View.inflate(this, R.layout.progress_dialog_install, null)
        val builder: AlertDialog.Builder = if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog_Black)
        } else {
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        }
        builder.setView(inflate)
        val dialog = builder.create()

        if (uninstall) {
            inflate.installProgressTxt.setText(R.string.progress_uninstalling_title)
        }

        val filter = IntentFilter(Notifier.ACTION_FAILED)
        filter.addAction(Notifier.ACTION_INSTALLED)
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(installListener, filter)

        progressBar = inflate.installProgressBar
        progressBar.isIndeterminate = uninstall
        progressCount = inflate.installProgressCount
        progressPercent = inflate.installProgressPercent

        if (!uninstall) {
            updateProgress("", 0, apps.size - 1, uninstall)
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
            RomInfo.getRomInfo(this).postInstall(true, apps, null)
        }
        if (!uninstall) {
            InstallerServiceHelper.install(this, apps)
        }
    }

    override fun recreate() {
        //super.recreate()
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
                Log.d("TEST", "package failed - ${intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)}")
                errorMap[intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME)] =
                        intent.getStringExtra(Notifier.EXTRA_LOG)
            }
        }

    }
}