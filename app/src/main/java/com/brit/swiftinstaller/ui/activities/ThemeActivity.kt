package com.brit.swiftinstaller.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getBackgroundColor

 @SuppressLint("Registered")
 open class ThemeActivity: AppCompatActivity() {
     private var mBlackBackground = false
    override fun onCreate(savedInstanceState: Bundle?) {
        mBlackBackground = getBackgroundColor(this) == 0x000000
        if (mBlackBackground) {
            setTheme(R.style.AppTheme_Black)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
    }

     override fun onResume() {
         super.onResume()
         if ((getBackgroundColor(this) == 0x000000) != mBlackBackground) {
             mBlackBackground = getBackgroundColor(this) == 0x000000
             recreate()
         }
     }
}