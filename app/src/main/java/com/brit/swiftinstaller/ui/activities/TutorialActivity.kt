package com.brit.swiftinstaller.ui.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getBackgroundColor
import com.brit.swiftinstaller.library.R
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import org.jetbrains.anko.doAsync

class TutorialActivity : TutorialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!resources.getBoolean(R.bool.allow_unsupported_systems)) {
            val samsung = packageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA).metaData.getBoolean("is_samsung_only", false)
            if (samsung && !packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile")) {
                AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                        .setTitle("Unsupported")
                        .setMessage("Only supports samsung devices for now.")
                        .setPositiveButton("EXIT") { _, _ ->
                            finish()
                        }
                        .show()
                return
            }
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("appHasRunBefore", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            doAsync {
                overlaysList = Utils.sortedOverlaysList(this@TutorialActivity)
            }

            setIndicator(R.drawable.tutorial_indicator)
            setIndicatorSelected(R.drawable.tutorial_indicator_selected)

            RomInfo.getRomInfo(this).addTutorialSteps(this)
        }
    }

    override fun finishTutorial() {
        super.finishTutorial()
        val intent = Intent(this, CustomizeActivity::class.java)
        intent.putExtra("parentActivity", "tutorial")
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("appHasRunBefore", true).apply()
        val bundle = Bundle()
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }
}