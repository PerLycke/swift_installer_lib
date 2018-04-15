package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.useBlackBackground

 @SuppressLint("Registered")
 open class ThemeActivity: AppCompatActivity() {
     private var mBlackBackground = false
    override fun onCreate(savedInstanceState: Bundle?) {
        mBlackBackground = useBlackBackground(this)
        if (mBlackBackground) {
            setTheme(R.style.AppTheme_Black)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
    }

     override fun onResume() {
         super.onResume()
         if (useBlackBackground(this) != mBlackBackground) {
             mBlackBackground = useBlackBackground(this)
             recreate()
         }
     }
}