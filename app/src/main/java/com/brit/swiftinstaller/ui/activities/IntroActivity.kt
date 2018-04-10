package com.brit.swiftinstaller.ui.activities

import android.Manifest
import android.os.Bundle
import com.brit.swiftinstaller.R
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.hololo.tutorial.library.PermissionStep

class IntroActivity : TutorialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("appHasRunBefore", false)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            setIndicator(R.drawable.intro_indicator)
            setIndicatorSelected(R.drawable.intro_indicator_selected)

            addFragment(Step.Builder().setTitle(resources.getString(R.string.intro_title))
                    .setContent(resources.getString(R.string.intro_guide))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_intro_logo) // int top drawable
                    .build())
            addFragment(Step.Builder().setTitle(resources.getString(R.string.intro_customize_title))
                    .setContent(resources.getString(R.string.intro_customize_desc))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main))
                    .setDrawable(R.drawable.ic_intro_customize) // int top drawable
                    .build())
            addFragment(PermissionStep.Builder().setTitle(getString(R.string.permission_title))
                    .setContent(getString(R.string.permission_detail))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.background_main)) // int background color
                    .setDrawable(R.drawable.ic_intro_permission)
                    .setPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .build())
        }
    }

    override fun finishTutorial() {
        super.finishTutorial()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("appHasRunBefore", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}