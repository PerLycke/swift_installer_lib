package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.useBlackBackground

 @SuppressLint("Registered")
 open class ThemeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (useBlackBackground(this)) {
            setTheme(R.style.AppTheme_Black)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener({ _, key ->
            if (key == "black_background") {
                recreate()
            }
        })
    }
}