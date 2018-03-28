package com.brit.swiftinstaller.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getEnterpriseKey
import com.brit.swiftinstaller.utils.getKnoxKey
import com.brit.swiftinstaller.utils.setEnterpriseKey
import com.brit.swiftinstaller.utils.setKnoxKey
import kotlinx.android.synthetic.main.knox.*

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_run", true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_run", false).apply()
            setContentView(R.layout.knox)
        } else {
            startMainActivity()
            return
        }

        knox.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Setup Knox Key")
            val view = View.inflate(this, R.layout.knox_dialog, null)
            val knoxKey = view.findViewById<EditText>(R.id.knox_key)
            val enterpriseKey = view.findViewById<EditText>(R.id.enterprise_key)
            knoxKey.setText(getKnoxKey(this))
            enterpriseKey.setText(getEnterpriseKey(this))
            builder.setView(view)
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        setKnoxKey(this, knoxKey.text.toString())
                        setEnterpriseKey(this, enterpriseKey.text.toString())
                        startMainActivity()
                    }
            builder.show()
        }
    }

    fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}