package com.brit.swiftinstaller.ui.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getBackgroundColor
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity

class TutorialActivity : TutorialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!resources.getBoolean(R.bool.allow_unsupported_systems)) {
            if (!packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile")) {
                AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                        .setTitle("Unsupported")
                        .setMessage("Only supports samsung devices for now.")
                        .setPositiveButton("EXIT", { _, _ ->
                            finish()
                        })
                        .show()
                return
            }
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("appHasRunBefore", false)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            setIndicator(R.drawable.tutorial_indicator)
            setIndicatorSelected(R.drawable.tutorial_indicator_selected)

            addFragment(Step.Builder().setTitle(resources.getString(R.string.app_name))
                    .setContent(resources.getString(R.string.tutorial_guide))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_tutorial_logo) // int top drawable
                    .build())
            addFragment(Step.Builder().setTitle(resources.getString(R.string.tutorial_apps_title))
                    .setContent(resources.getString(R.string.tutorial_apps))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_apps) // int top drawable
                    .build())
            addFragment(Step.Builder().setTitle(getString(R.string.basic_usage))
                    .setContent(resources.getString(R.string.tutorial_basic_usage_content))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_tutorial_hand) // int top drawable
                    .build())
            addFragment(Step.Builder().setTitle(getString(R.string.tutorial_more_usage_title))
                    .setContent(getString(R.string.tutorial_more_usage_info))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_tutorial_clicks) // int top drawable
                    .build())
            addFragment(PermissionStep.Builder().setTitle(getString(R.string.tutorial_permission_title))
                    .setContent(getString(R.string.tutorial_permission_content))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main)) // int background color
                    .setDrawable(R.drawable.ic_tutorial_permission)
                    .setPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .build())
            addFragment(Step.Builder().setTitle(resources.getString(R.string.tutorial_customize_title))
                    .setContent(resources.getString(R.string.tutorial_customize_content))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_tutorial_customize) // int top drawable
                    .build())
        }
    }

    override fun finishTutorial() {
        super.finishTutorial()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("appHasRunBefore", true).apply()
        startActivity(Intent(this, CustomizeActivity::class.java).putExtra("parentActivity", "tutorial"))
        finish()
    }
}