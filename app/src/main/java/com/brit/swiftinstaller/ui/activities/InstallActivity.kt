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
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.installer.Notifier
import com.brit.swiftinstaller.utils.InstallerHandler
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import kotlinx.android.synthetic.main.install_progress_sheet.*


@Suppress("UNUSED_PARAMETER")
class InstallActivity : AppCompatActivity() {

    private lateinit var mProgressBar: ProgressBar

    fun updateProgress(label: String?, progress: Int, max: Int, uninstall: Boolean) {

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

        mProgressBar = installProgressBar

        InstallerServiceHelper.setInstallerCallback(object : IInstallerCallback.Stub() {
            override fun installComplete(uninstall: Boolean) {
            }

            override fun installFailed(reason: Int) {
            }

            override fun progressUpdate(label: String?, progress: Int, max: Int, uninstall: Boolean) {
                updateProgress(label, progress, max, uninstall)
            }

            override fun installStarted() {
            }

        })
        InstallerServiceHelper.install(intent.getStringArrayListExtra("apps"))
    }
}