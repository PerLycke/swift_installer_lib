package com.brit.swiftinstaller.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.UpdateChecker
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.getAppsToUpdate
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "overlays_to_update") {
                val apps = getAppsToUpdate(this)
                if (apps.isEmpty()) {
                    installUpdatesTile.visibility = View.GONE
                } else {
                    installUpdatesTile.visibility = View.VISIBLE
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        installTile.setOnClickListener {
            startActivity(Intent(this, OverlaysActivity::class.java))
        }

        updateTileLayout.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            intent.putExtra("tab", OverlaysActivity.UPDATE_TAB)
            startActivity(intent)
        }

        accentTile.setOnClickListener {
            val intent = Intent(this, CustomizeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0]
                                != PackageManager.PERMISSION_GRANTED)) {
                    // Permission denied show error dialog and exit
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentAccent.setTextColor(getAccentColor(this))
        currentAccent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                activeCount.text = String.format("%d", installedCount)
                if (updates.isEmpty()) {
                    updateTileLayout.visibility = View.GONE
                } else {
                    updatesCount.text = String.format("%d", updates.size)
                    updateTileLayout.visibility = View.VISIBLE
                }
            }

        }).execute()
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
            R.id.disable_notifications -> {
                val dialogView = View.inflate(this, R.layout.dialog_disable_notifications, null)
                val builder = if (AppCompatDelegate.getDefaultNightMode()
                        == AppCompatDelegate.MODE_NIGHT_YES) {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog_Black)
                } else {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                }
                builder.setView(dialogView)
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.action_about -> {
                val dialogView = View.inflate(this, R.layout.dialog_about, null)
                val builder = if (AppCompatDelegate.getDefaultNightMode()
                        == AppCompatDelegate.MODE_NIGHT_YES) {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog_Black)
                } else {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                }
                builder.setView(dialogView)
                val dialog = builder.create()

                dialogView.aboutOkBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
