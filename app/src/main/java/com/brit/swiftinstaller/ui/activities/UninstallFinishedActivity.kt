package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.runCommand

class UninstallFinishedActivity : ThemeActivity() {

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallActivity().finish()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
        .setTitle(R.string.reboot)
        .setMessage(R.string.reboot_manually)
        if (!ShellUtils.isRootAvailable) {
            builder.setPositiveButton(R.string.reboot_later) { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }
        } else {
            builder.setPositiveButton("Reboot Now") { dialogInterface, _ ->
                runCommand("setprop ctl.restart zygote", true)
                finish()
            }
            builder.setNegativeButton("Reboot Later") { dialogInterface, _ ->
                finish()
            }
        }

        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        themeDialog()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            dialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}