package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.rebootCommand

class RebootActivity : ThemeActivity() {

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("reboot_card", false).apply()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(getString(R.string.reboot_dialog_title))
                .setMessage(getString(R.string.reboot_dialog_msg))
                .setPositiveButton(getString(R.string.reboot)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val dialog = Dialog(this, R.style.AppTheme_Translucent)
                    dialog.setContentView(R.layout.reboot)
                    dialog.show()
                    mHandler.post {
                        rebootCommand()
                    }
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