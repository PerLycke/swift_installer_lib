package com.brit.swiftinstaller.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.installer.Notifier
import com.brit.swiftinstaller.utils.InstallerHandler
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import kotlinx.android.synthetic.main.install_progress_sheet.*


@Suppress("UNUSED_PARAMETER")
class InstallActivity : AppCompatActivity() {

    private lateinit var mProgressBar: ProgressBar

    fun progressUpdate(label: String?, progress: Int, max: Int, uninstall: Boolean) {

        Handler(Looper.getMainLooper()).post({
            if (mProgressBar.progress < progress) {
                mProgressBar.progress = progress
                mProgressBar.max = max
                main_content.invalidate()
                mProgressBar.postInvalidate()
                installProgressCount.text = getString(R.string.install_count, progress, max)
                installProgressPercent.text = String.format("%.0f%%", (progress * 100 / max))
            }
            Log.d("TEST", "progress - $progress/$max")
            if (progress == max) {
                installComplete(false)
            }
        })
    }

    fun installComplete(uninstall: Boolean) {
        finish()
    }

    fun installFailed(reason: Int) {
        // TODO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_progress_sheet)
        InstallerServiceHelper.connectService(this)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when {
                    intent!!.action == InstallerHandler.INSTALL_PROGRESS -> {
                        progressUpdate(intent.getStringExtra("label"), intent.getIntExtra("progress", 0), intent.getIntExtra("max", 0), false)
                    }
                    intent.action == InstallerHandler.INSTALL_COMPLETE -> {
                        installComplete(intent.getBooleanExtra("uninstall", false))
                    }
                    intent.action == Notifier.ACTION_INSTALLED -> {
                        val packageName = intent.extras.getString(Notifier.EXTRA_PACKAGE_NAME)
                        progressUpdate(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager) as String,
                                intent.getIntExtra(Notifier.EXTRA_PROGRESS, 0), intent.getIntExtra(Notifier.EXTRA_MAX, 0), false)
                    }
                    intent.action == Notifier.ACTION_INSTALL_STARTED -> {
                        progressUpdate(null, 0, intent.getIntExtra(Notifier.EXTRA_MAX, 0), false)
                    }
                    intent.action == Notifier.ACTION_FAILED -> {
                        installFailed(intent.getIntExtra(Notifier.EXTRA_REASON, 0))
                    }
                }
            }

        }

        val filter = IntentFilter(Notifier.ACTION_INSTALL_FINISHED)
        filter.addAction(Notifier.ACTION_INSTALL_STARTED)
        filter.addAction(Notifier.ACTION_INSTALLED)
        filter.addAction(Notifier.ACTION_FAILED)

        LocalBroadcastManager.getInstance(this.applicationContext).registerReceiver(receiver, filter)

        mProgressBar = installProgressBar

        InstallerServiceHelper.install(intent.getStringArrayListExtra("apps"))
    }
}