package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.os.PowerManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import kotlinx.android.synthetic.main.alert_dialog_uninstall_reboot.view.*
import kotlinx.android.synthetic.main.sheet_install_summary_fab.view.*

class UninstallFinishedActivity: AppCompatActivity() {

    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflate = View.inflate(this, R.layout.alert_dialog_uninstall_reboot, null)
        val builder: AlertDialog.Builder
        if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme_Black)
        } else {
            builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
        }
        builder.setView(inflate)
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        inflate.rebootUninstallBtn.setOnClickListener {
            dialog.dismiss()
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            dialog.show()
        }
    }
}