package com.brit.swiftinstaller.utils

import android.support.v7.app.AppCompatActivity

class EnableOverlaysActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        Utils.enableAllOverlays(this)
    }
}