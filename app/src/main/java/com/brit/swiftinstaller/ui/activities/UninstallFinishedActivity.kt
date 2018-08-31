package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.brit.swiftinstaller.R

class UninstallFinishedActivity : ThemeActivity() {

    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallActivity().finish()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        themeDialog()

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