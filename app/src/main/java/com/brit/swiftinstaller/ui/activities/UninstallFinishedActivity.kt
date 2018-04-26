package com.brit.swiftinstaller.ui.activities

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getBackgroundColor

class UninstallFinishedActivity: AppCompatActivity() {

    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)

        builder.setTitle(R.string.reboot)
        builder.setMessage(R.string.reboot_manually)
        builder.setPositiveButton(R.string.reboot_later, { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
        })

        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            dialog.show()
        }
    }
}