package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.runCommand

class RebootActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("should_notify", false).apply()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(getString(R.string.reboot_dialog_title))
                .setMessage(getString(R.string.reboot_dialog_msg))
                .setPositiveButton(getString(R.string.reboot)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    runCommand("setprop ctl.restart zygote", true)
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    finish()
                }
                .setOnCancelListener {
                    finish()
                }

        themeDialog()
        val dialog = builder.create()
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}