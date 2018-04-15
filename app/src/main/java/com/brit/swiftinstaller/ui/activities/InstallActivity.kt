package com.brit.swiftinstaller.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.utils.InstallerServiceHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.addAppToUninstall
import com.brit.swiftinstaller.utils.rom.RomInfo
import kotlinx.android.synthetic.main.sheet_install_progress.view.*
import java.util.ArrayList


@Suppress("UNUSED_PARAMETER")
class InstallActivity : ThemeActivity() {

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressCount: TextView
    private lateinit var mProgressPercent: TextView

    private var mUninstall = false

    private lateinit var mApps: ArrayList<String>

    val errorMap: HashMap<String, String> = HashMap()

    fun updateProgress(label: String?, prog: Int, maximum: Int, uninstall: Boolean) {
        val max = maximum + 1
        val progress = prog + 1
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

    private fun installComplete(uninstall: Boolean) {
        if (!uninstall) {
            val intent = Intent(this, InstallSummaryActivity::class.java)
            intent.putExtra("errorMap", Utils.mapToBundle(errorMap))
            intent.putExtra("apps", mApps)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            RomInfo.getRomInfo(this).postInstall(uninstall, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUninstall = intent.extras.getBoolean("uninstall", false)
        mApps = intent.getStringArrayListExtra("apps")
        mApps.forEach { Log.d("TEST", "install $it") }

        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_install_progress, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.setOnCancelListener {
            finish()
        }

        if (mUninstall) {
            sheetView.installProgressTxt.setText(R.string.uninstalling_overlays)
        }

        mProgressBar = sheetView.installProgressBar
        mProgressBar.isIndeterminate = mUninstall
        mProgressCount = sheetView.installProgressCount
        mProgressPercent = sheetView.installProgressPercent

        if (!mUninstall) {
            updateProgress("", 0, mApps.size - 1, mUninstall)
        } else {
            mProgressCount.visibility = View.INVISIBLE
            mProgressPercent.visibility = View.INVISIBLE
        }
        bottomSheetDialog.show()

        if (mUninstall && !ShellUtils.isRootAvailable) {
            val filter = IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            filter.addDataScheme("package")
            registerReceiver(object : BroadcastReceiver() {
                var count = mApps.size
                override fun onReceive(context: Context?, intent: Intent?) {
                    count--
                    if (count == 0) {
                        startActivity(Intent(this@InstallActivity,
                                UninstallFinishedActivity::class.java))
                        finish()
                    }
                }
            }, filter)
            mApps.forEach {
                addAppToUninstall(this, it)
                Log.d("TEST", "uninstall $it")
            }
            RomInfo.getRomInfo(this).postInstall(true, null)
        }

        InstallerServiceHelper.connectService(this)

        InstallerServiceHelper.setInstallerCallback(object : IInstallerCallback.Stub() {
            override fun installComplete(uninstall: Boolean) {
            }

            override fun installFailed(errorLog: String, packageName: String) {
                errorMap[packageName] = errorLog
            }

            override fun progressUpdate(label: String?, progress: Int, max: Int, uninstall: Boolean) {
                updateProgress(label, progress, max, uninstall)
            }

            override fun installStarted() {
            }

        })
        if (!mUninstall) {
            InstallerServiceHelper.install(mApps)
        }
    }
}