package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.rom.RomInfo
import kotlinx.android.synthetic.main.install_progress_sheet.view.*


@Suppress("UNUSED_PARAMETER")
class InstallActivity : AppCompatActivity() {

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressCount: TextView
    private lateinit var mProgressPercent: TextView

    fun updateProgress(label: String?, progress: Int, max: Int, uninstall: Boolean) {
        if (mProgressBar.progress < progress) {
            mProgressBar.progress = progress
            mProgressBar.max = max
            mProgressBar.postInvalidate()
            mProgressCount.text = getString(R.string.install_count, progress, max)
            mProgressPercent.text = String.format("%.0f%%", ((progress * 100 / max) + 0.0f))
        }
        Log.d("TEST", "progress - $progress/$max")
        if (progress == max) {
            installComplete(uninstall)
        }
    }

    fun installComplete(uninstall: Boolean) {
        finish()
        RomInfo.getRomInfo(this).postInstall(uninstall)
    }

    fun installFailed(reason: Int) {
        // TODO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uninstall = intent.extras.getBoolean("uninstall", false)
        val apps = intent.getStringArrayListExtra("apps")

        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = LayoutInflater.from(this).inflate(R.layout.install_progress_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
        mBottomSheetDialog.setOnCancelListener {
            finish()
        }
        InstallerServiceHelper.connectService(this)

        if (uninstall) {
            sheetView.findViewById<TextView>(R.id.installProgressTxt).setText(R.string.uninstalling_overlays)
        }

        mProgressBar = sheetView.installProgressBar
        mProgressCount = sheetView.installProgressCount
        mProgressPercent = sheetView.installProgressPercent

        updateProgress("", 0, apps.size, uninstall)

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
        if (uninstall) {
            InstallerServiceHelper.uninstall(apps)
        } else {
            InstallerServiceHelper.install(apps)
        }
    }
}