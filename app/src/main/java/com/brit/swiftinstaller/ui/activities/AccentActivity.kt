package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.setAccentColor
import com.enrico.colorpicker.ColorPickerDialog.ColorPickerDialogListener
import com.enrico.colorpicker.ColorPickerDialogFragment.showColorPicker

class AccentActivity : AppCompatActivity(), ColorPickerDialogListener {
    override fun dismiss() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showColorPicker(this, this, getAccentColor(this), "")
    }

    override fun colorPicked(key: String?, color: Int) {
        setAccentColor(this, color)
        finish()
    }
}
