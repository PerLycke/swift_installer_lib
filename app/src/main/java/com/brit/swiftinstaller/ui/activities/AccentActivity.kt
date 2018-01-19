package com.brit.swiftinstaller.ui.activities

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.brit.swiftinstaller.utils.addAccentColor
import com.brit.swiftinstaller.utils.setAccentColor

class AccentActivity : AppCompatActivity() {

    fun setAccent(view: View) {
        Toast.makeText(this, "Accent updated", Toast.LENGTH_SHORT).show()
        finish()
    }
}
