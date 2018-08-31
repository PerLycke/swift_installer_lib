package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.OverlayUtils
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getBackgroundColor
import com.brit.swiftinstaller.library.R
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.brit.swiftinstaller.utils.getProperty
import com.hololo.tutorial.library.TutorialActivity
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.no_root.view.*
import org.jetbrains.anko.doAsync

class TutorialActivity : TutorialActivity() {

    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ShellUtils.isRootAvailable) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel(
                    "com.brit.swiftinstaller",
                    "Swift overlays enabling",
                    "The notification notifies you when overlays are being enabled on boot")
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("appHasRunBefore", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {

            if (!Utils.isSamsungOreo(this) && !ShellUtils.isRootAvailable) {
                val dialog = Dialog(this, R.style.AppTheme)
                val layout = LayoutInflater.from(this).inflate(R.layout.no_root, null)
                dialog.setContentView(layout)
                dialog.show()
                dialog.setCancelable(false)
                layout.no_root_exit.setOnClickListener {
                    finish()
                }
                return
            }

            doAsync {
                OverlayUtils.checkAndHideOverlays(this@TutorialActivity)
            }

            setIndicator(R.drawable.tutorial_indicator)
            setIndicatorSelected(R.drawable.tutorial_indicator_selected)

            RomInfo.getRomInfo(this).addTutorialSteps(this)
        }
    }

    override fun finishTutorial() {
        super.finishTutorial()
        val intent = Intent(this, CustomizeActivity::class.java)
        intent.putExtra("parentActivity", "tutorial")
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("appHasRunBefore", true).apply()
        startActivity(intent)
        finish()
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        notificationManager?.createNotificationChannel(channel)
    }
}