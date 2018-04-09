package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.os.PowerManager
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.addAppToUninstall
import com.brit.swiftinstaller.utils.rom.RomInfo
import kotlinx.android.synthetic.main.install_progress_sheet.view.*


@Suppress("UNUSED_PARAMETER")
class InstallActivity : AppCompatActivity() {

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressCount: TextView
    private lateinit var mProgressPercent: TextView

    private var mUninstall = false

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
        RomInfo.getRomInfo(this).postInstall(uninstall)

        /*if (RomInfo.getRomInfo(this).shouldReboot()) {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
            val sheetView = View.inflate(this, R.layout.reboot_sheet, null)
            bottomSheetDialog.setContentView(sheetView)
            bottomSheetDialog.show()

            val rebootNow = sheetView.findViewById<View>(R.id.textView3)
            rebootNow.setOnClickListener {
                val pm = getSystemService(PowerManager::class.java)
                pm.reboot(null)
                bottomSheetDialog.dismiss()
            }
            val rebootLater = sheetView.findViewById<View>(R.id.textView4)
            rebootLater.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        } else {*/
            finish()
//        }
    }

    fun installFailed(reason: Int) {
        // TODO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_Black);
        }
        super.onCreate(savedInstanceState)
        mUninstall = intent.extras.getBoolean("uninstall", false)
        val apps = intent.getStringArrayListExtra("apps")

        if (mUninstall && !ShellUtils.isRootAvailable) {
            apps.forEach {
                addAppToUninstall(this, it)
                Log.d("TEST", "uninstall $it")
            }
            RomInfo.getRomInfo(this).postInstall(true)
            return
        }

        val mBottomSheetDialog: BottomSheetDialog
        if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme_Black)
        } else {
            mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        }
        val sheetView = LayoutInflater.from(this).inflate(R.layout.install_progress_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
        mBottomSheetDialog.setOnCancelListener {
            finish()
        }
        InstallerServiceHelper.connectService(this)

        if (mUninstall) {
            sheetView.findViewById<TextView>(R.id.installProgressTxt).setText(R.string.uninstalling_overlays)
        }

        mProgressBar = sheetView.installProgressBar
        mProgressCount = sheetView.installProgressCount
        mProgressPercent = sheetView.installProgressPercent

        updateProgress("", 0, apps.size, mUninstall)

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
        if (mUninstall) {
            InstallerServiceHelper.uninstall(apps)
        } else {
            InstallerServiceHelper.install(apps)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mUninstall)
            finish()
    }
}