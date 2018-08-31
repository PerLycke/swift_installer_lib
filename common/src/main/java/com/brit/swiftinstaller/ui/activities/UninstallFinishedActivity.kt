package com.brit.swiftinstaller.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.rebootCommand

class UninstallFinishedActivity : ThemeActivity() {

    private lateinit var dialog: AlertDialog
    private val mHandler = Handler()

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
            builder.setPositiveButton("Reboot Now") { _, _ ->
                val dialog = Dialog(this, R.style.AppTheme_Translucent)
                dialog.setContentView(R.layout.reboot)
                dialog.show()
                mHandler.post {
                    rebootCommand()
                }
            }
            builder.setNegativeButton("Reboot Later") { _, _ ->
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