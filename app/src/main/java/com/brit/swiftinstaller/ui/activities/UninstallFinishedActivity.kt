package com.brit.swiftinstaller.ui.activities

import android.os.Bundle
import android.os.PowerManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import kotlinx.android.synthetic.main.sheet_install_summary_fab.view.*

class UninstallFinishedActivity: AppCompatActivity() {

    lateinit var bottomSheetDialog: ThemedBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_install_summary_fab, null)

        sheetView.sendLog.visibility = View.GONE

        sheetView.reboot.setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            bottomSheetDialog.dismiss()
            pm.reboot(null)
        }

        sheetView.rebootLater.setOnClickListener {
            bottomSheetDialog.dismiss()
            finish()
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.setOnCancelListener {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            bottomSheetDialog.show()
        }
    }
}