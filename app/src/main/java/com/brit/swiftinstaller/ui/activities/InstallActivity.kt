package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import kotlinx.android.synthetic.main.install_progress_sheet.*
import kotlinx.android.synthetic.main.install_progress_sheet.view.*


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
        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = LayoutInflater.from(this).inflate(R.layout.install_progress_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
        mBottomSheetDialog.setOnCancelListener {
            finish()
        }
        InstallerServiceHelper.connectService(this)

        mProgressBar = sheetView.installProgressBar

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