package com.brit.swiftinstaller.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog

class InstallSummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_summary)
    }

    fun rebootActions(view: View) {
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_install_summary_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }
}
