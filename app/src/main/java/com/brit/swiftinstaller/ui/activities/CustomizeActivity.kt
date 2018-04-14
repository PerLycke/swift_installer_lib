package com.brit.swiftinstaller.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable
import com.brit.swiftinstaller.utils.*
import kotlinx.android.synthetic.main.customize_accent.*
import kotlinx.android.synthetic.main.alert_dialog_background.view.*
import kotlinx.android.synthetic.main.customize_background.*
import kotlinx.android.synthetic.main.customize_preview.*
import kotlinx.android.synthetic.main.toolbar_customize.*

class CustomizeActivity : AppCompatActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(3)

    private var mAccent: Int = 0

    private var mFinish = false
    private var mBlackBackround = false

    override fun onCreate(savedInstanceState: Bundle?) {
        mBlackBackround = useBlackBackground(this)
        if (savedInstanceState != null) {
            mBlackBackround = savedInstanceState.getBoolean("black", mBlackBackround)
        }
        if (mBlackBackround) {
            setTheme(R.style.AppTheme_Black);
        }
        super.onCreate(savedInstanceState)
        mAccent = getAccentColor(this)
        setContentView(R.layout.activity_customize)
        setupAccentSheet()
        updateColor(mAccent)
        setBgIndicator()



        customizeConfirmBtn.setOnClickListener {
            val apps = ArrayList<String>()
            if (getAccentColor(this) != mAccent) {
                setAccentColor(this, mAccent)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    apps.add("android")
                }
            }

            if (mBlackBackround != useBlackBackground(this)) {
                setUseBlackBackground(this, mBlackBackround)
                if (apps.contains("android")) {
                    apps.remove("android")
                }
                apps.addAll(Utils.getInstalledOverlays(this))
            }

            if (useBlackBackground(this)) {
                AppCompatDelegate
                        .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate
                        .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            if (!apps.isEmpty()) {
                mFinish = true
                val intent = Intent(this, InstallActivity::class.java)
                intent.putStringArrayListExtra("apps", apps)
                startActivity(intent)
            } else {
                finish()
            }
        }

        hexInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0)
                    updateColor(Utils.convertToColorInt(s.toString()))
            }

        })

        applyHex.setOnClickListener {
            updateColor(Utils.convertToColorInt(hexInput.text.toString()))
        }
    }

    override fun onResume() {
        super.onResume()
        if (mFinish) finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("black", mBlackBackround)
    }

    private fun setupAccentSheet() {
        palette.adapter = PaletteAdapter(resources.getIntArray(R.array.accent_colors))
        settingsIcons[0] = connectionsIcon
        settingsIcons[1] = soundIcon
        settingsIcons[2] = notificationsIcon
    }

    private fun setBgIndicator() {
        if (mBlackBackround) {
            blackBgIndicator.visibility = View.VISIBLE
            darkBgIndicator.visibility = View.GONE
            val settingsBg = ContextCompat.getDrawable(this, R.drawable.settings_preview_black)
            settingsPreview.setImageDrawable(settingsBg)
        } else {
            darkBgIndicator.visibility = View.VISIBLE
            blackBgIndicator.visibility = View.GONE
            val settingsBg = ContextCompat.getDrawable(this, R.drawable.settings_preview_dark)
            settingsPreview.setImageDrawable(settingsBg)
        }
        darkBgIndicator.invalidate()
        blackBgIndicator.invalidate()
    }

    fun updateColor(color: Int) {
        mAccent = color
        for (icon: ImageView? in settingsIcons) {
            icon!!.setColorFilter(color)
        }
        hexInput.background.setTint(color)
        //hexInput.setText(Integer.toString(color), TextView.BufferType.EDITABLE)
        applyHex.setTextColor(color)
    }

    inner class PaletteAdapter constructor(private val mColors: IntArray) : BaseAdapter() {

        override fun getCount(): Int {
            return mColors.size
        }

        override fun getItem(position: Int): Any {
            return mColors[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var mainView = convertView
            if (mainView == null) {
                mainView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.palette_view, parent, false)
            }
            val iv = mainView!!.findViewById<ImageView>(R.id.icon)
            iv.background = CircleDrawable(mColors[position])
            mainView.tag = mColors[position]
            mainView.setOnClickListener {
                hexInput.setText("", TextView.BufferType.EDITABLE)
                updateColor(mColors[position])
            }
            return mainView
        }
    }

    fun bgClick(view: View) {
        val dialogView = View.inflate(this, R.layout.alert_dialog_background, null)
        val builder: AlertDialog.Builder
        if (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES) {
            builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme_Black)
        } else {
            builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
        }
        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.bgContinueBtn.setOnClickListener {
            mBlackBackround = view.id == R.id.blackBgCircle
            setBgIndicator()
            recreate()
            dialog.dismiss()
        }

        dialogView.bgBackBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun cancelBtnClick(view: View) {
        finish()
    }

}
