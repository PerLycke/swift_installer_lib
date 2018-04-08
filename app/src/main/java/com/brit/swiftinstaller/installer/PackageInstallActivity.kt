package com.brit.swiftinstaller.installer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.clearAppsToInstall
import com.brit.swiftinstaller.utils.clearAppsToUninstall
import java.io.File

class PackageInstallActivity : AppCompatActivity() {

    private lateinit var mApps : Array<String>
    private var mUninstall = false

    private var mIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mApps = intent.getStringArrayExtra("apps")
        mUninstall = intent.getBooleanExtra("uninstall", false)
    }

    override fun onResume() {
        super.onResume()

        if (mUninstall)
            continueUninstall()
        else
            continueInstall()
    }

    private fun continueUninstall() {
        if (mIndex == mApps.size) {
            clearAppsToUninstall(this)
            finish()
            return
        }
        if (!Utils.isOverlayInstalled(this, Utils.getOverlayPackageName(mApps[mIndex]))) {
            mIndex++
            continueUninstall()
        } else {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.fromParts("package", mApps[mIndex], null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            mIndex++
        }
    }

    private fun continueInstall() {
        if (mIndex == mApps.size) {
            clearAppsToInstall(this)
            finish()
            return
        }
        Log.d("TEST", "app path - " + mApps[mIndex])
        if (!File(mApps[mIndex]).exists()) {
            mIndex++
            continueInstall()
        } else {
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.setDataAndType(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".myprovider",
                    File(mApps[mIndex])), "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
            mIndex++
        }
    }
}