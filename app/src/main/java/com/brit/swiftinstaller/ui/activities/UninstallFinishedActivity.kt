package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.brit.swiftinstaller.R

class UninstallFinishedActivity: AppCompatActivity() {

    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder: AlertDialog.Builder
        builder = if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog_Black)
        } else {
            AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        }
                .setTitle(R.string.reboot)
                .setMessage(R.string.reboot_manually)
                .setPositiveButton(R.string.reboot_later, { dialogInterface, i ->
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