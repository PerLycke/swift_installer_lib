package com.brit.swiftinstaller.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.MaterialPalette
import com.brit.swiftinstaller.utils.UpdateChecker
import com.brit.swiftinstaller.utils.Utils
import kotlinx.android.synthetic.main.card_compatibility_info.*
import kotlinx.android.synthetic.main.card_install.*
import kotlinx.android.synthetic.main.card_update.*
import com.brit.swiftinstaller.utils.getAppsToUpdate
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.popup_menu.view.*
import org.jetbrains.anko.doAsync

class MainActivity : ThemeActivity() {

    private var overlaysList = ArrayList<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        doAsync {
            Utils.enableAllOverlays(this@MainActivity)
            overlaysList = Utils.sortedOverlaysList(this@MainActivity)
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("not_closed", true)) {
            card_compatibility.visibility = View.VISIBLE
            card_compatibility_close.setOnClickListener {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("not_closed", false).apply()
                card_compatibility.visibility = View.GONE
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        101)
            }
        }

        card_install.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList("overlays_list", overlaysList)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        card_update.setOnClickListener {
            val intent = Intent(this, OverlaysActivity::class.java)
            intent.putExtra("tab", OverlaysActivity.UPDATE_TAB)
            val bundle = Bundle()
            bundle.putParcelableArrayList("overlays_list", overlaysList)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        card_personalize.setOnClickListener {
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

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                active_count.text = String.format("%d", installedCount)
                if (updates.isEmpty()) {
                    card_update.visibility = View.GONE
                } else {
                    updates_count.text = String.format("%d", updates.size)
                    card_update.visibility = View.VISIBLE
                }
            }

        }).execute()
    }

    @SuppressLint("RtlHardcoded", "InflateParams")
    fun overflowClick(view: View) {
        val popup = PopupWindow(this, null, 0, R.style.PopupWindow)
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu, null)
        popup.animationStyle = R.style.PopupWindowAnimation
        popup.contentView = popupView
        popup.isFocusable = true

        val b = popupView.background as LayerDrawable
        b.findDrawableByLayerId(R.id.background_popup).setTint(MaterialPalette.get(this).cardBackgroud)

        popupView.popup_menu_about.setOnClickListener { _ ->
            popup.dismiss()

            val dialogView = View.inflate(this, R.layout.dialog_about, null)
            val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
            builder.setView(dialogView)
            themeDialog()

            val dialog = builder.create()

            dialogView.about_ok_btn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        popup.showAtLocation(view, Gravity.TOP or Gravity.RIGHT, 0, 0)
    }
}
