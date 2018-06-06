package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import com.brit.swiftinstaller.R

class UninstallFinishedActivity : ThemeActivity() {

    lateinit var dialog: AlertDialog
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallActivity().finish()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        .setTitle(R.string.reboot)
        .setMessage(R.string.reboot_manually)
        .setPositiveButton(R.string.reboot_later, { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
        })

        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        themeDialog()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            mHandler.post { dialog.show() }
        }
    }

    override fun onStop() {
        super.onStop()
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}