package com.brit.swiftinstaller.installer

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.clearAppsToInstall
import java.io.File

class PackageInstallActivity : AppCompatActivity() {

    private lateinit var mApps : Array<String>

    private var mIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mApps = intent.getStringArrayExtra("apps")
    }

    override fun onResume() {
        super.onResume()

        continueInstall();
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