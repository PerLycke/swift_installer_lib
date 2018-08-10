package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Environment
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.installer.Notifier
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import kotlinx.android.synthetic.main.progress_dialog_install.view.*
import java.io.File
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set


@Suppress("UNUSED_PARAMETER")
class InstallActivity : ThemeActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView
    private val installListener = InstallListener()

    private var uninstall = false
    private var update = false

    private var dialog: AlertDialog? = null

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

    private fun installComplete() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(installListener)
        val intent = Intent(this, InstallSummaryActivity::class.java)
        intent.putExtra("errorMap", Utils.mapToBundle(errorMap))
        intent.putExtra("update", update)
        File(Environment.getExternalStorageDirectory(), ".swift").deleteRecursively()
        errorMap.keys.forEach {
            if (apps.contains(it)) {
                apps.remove(it)
            }
        }
        intent.putExtra("apps", apps)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ShellUtils.isRootAvailable) {
            startActivity(intent)
        } else {
            RomInfo.getRomInfo(this).postInstall(false, apps, updateAppsToUninstall, intent)
        }
        finish()
    }

    fun uninstallComplete() {
        startActivity(Intent(this@InstallActivity,
                UninstallFinishedActivity::class.java))
        finish()
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
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        val fc = inflate.findViewById<TextView>(R.id.force_close)

        if (uninstall) {
            inflate.install_progress_txt.setText(R.string.progress_uninstalling_title)
            Handler().postDelayed({
                if (dialog != null && dialog?.isShowing!!) {
                    fc.visibility = View.VISIBLE
                    fc.setOnClickListener {
                        uninstallComplete()
                    }
                }
            }, 20000)
        }

        val filter = IntentFilter(Notifier.ACTION_FAILED)
        filter.addAction(Notifier.ACTION_INSTALLED)
        filter.addAction(Notifier.ACTION_INSTALL_COMPLETE)
        filter.addAction(Notifier.ACTION_UNINSTALLED)
        filter.addAction(Notifier.ACTION_UNINSTALL_COMPLETE)
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
        dialog?.show()

        if (uninstall) {
            if (!ShellUtils.isRootAvailable) {
                val intentfilter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                intentfilter.addDataScheme("package")
                registerReceiver(object : BroadcastReceiver() {
                    var count = apps.size
                    override fun onReceive(context: Context?, intent: Intent?) {
                        count--
                        if (count == 0) {
                            uninstallComplete()
                            context!!.unregisterReceiver(this)
                        }
                    }
                }, intentfilter)
                RomInfo.getRomInfo(this).postInstall(true, apps, null, null)
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
        // do nothing
    }

    override fun finish() {
        super.finish()
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
                        updateAppsToUninstall.add(intent.getStringExtra(Notifier.EXTRA_PACKAGE_NAME))
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