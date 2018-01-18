package com.brit.swiftinstaller.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getAccentColor
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        installTile.setOnClickListener {
            startActivity(Intent(this, OverlayActivity::class.java))
        }

        accentTile.setOnClickListener {
            startActivity(Intent(this, AccentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        currentAccent.setTextColor(getAccentColor(this))
        currentAccent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_about -> {
                val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
                builder.setTitle("About")
                builder.setMessage("This is about")
                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
