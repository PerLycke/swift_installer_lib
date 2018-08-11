package com.brit.swiftinstaller.utils

import android.app.Activity

class EnableOverlaysActivity : Activity() {

    override fun onResume() {
        super.onResume()
        Utils.enableAllOverlays(this)
        finish()
    }
}