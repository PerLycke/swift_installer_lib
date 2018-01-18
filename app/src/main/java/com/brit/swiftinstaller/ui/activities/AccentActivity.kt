package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.addAccentColor
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.getUserAccents
import com.brit.swiftinstaller.utils.setAccentColor
import com.enrico.colorpicker.ColorPickerDialog.ColorPickerDialogListener
import com.enrico.colorpicker.ColorPickerDialogFragment.showColorPicker

class AccentActivity : AppCompatActivity(), ColorPickerDialogListener {
    override fun dismiss() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accentDialog = LayoutInflater.from(this).inflate(R.layout.accent_dialog, null)
        val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
        builder.setView(accentDialog)
        builder.show()
        builder.setOnCancelListener {
            finish()
        }
    }

    override fun colorPicked(key: String?, color: Int) {
        setAccentColor(this, color)
        addAccentColor(this, color)
        finish()
    }

    fun setAccent(view: View) {
        Toast.makeText(this, "Accent updated", Toast.LENGTH_SHORT).show()
        finish()
    }
}
