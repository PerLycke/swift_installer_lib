package com.brit.swiftinstaller.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable
import com.brit.swiftinstaller.utils.*
import kotlinx.android.synthetic.main.accent_customize.*
import kotlinx.android.synthetic.main.activity_customize.*
import kotlinx.android.synthetic.main.background_alert_dialog.view.*
import kotlinx.android.synthetic.main.background_customize.*
import kotlinx.android.synthetic.main.customize_toolbar.*

class CustomizeActivity : AppCompatActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(3)

    private var mAccent: Int = 0

    private var mFinish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccent = getAccentColor(this)
        setContentView(R.layout.activity_customize)
        setupAccentSheet()
        updateColor(mAccent)
        setBgIndicator()

        customizeConfirmBtn.setOnClickListener {
            setAccentColor(this, mAccent)
            if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                mFinish = true
                val intent = Intent(this, InstallActivity::class.java)
                val apps = ArrayList<String>()
                apps.add("android")
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
                    updateColor(Integer.decode("0x$s"))
            }

        })

        applyHex.setOnClickListener {
            updateColor(Integer.decode("0x" + hexInput.text.toString()))
        }
    }

    override fun onResume() {
        super.onResume()
        if (mFinish) finish()
    }

    private fun setupAccentSheet() {
        palette.adapter = PaletteAdapter(resources.getIntArray(R.array.accent_colors))
        settingsIcons[0] = connectionsIcon
        settingsIcons[1] = soundIcon
        settingsIcons[2] = notificationsIcon
    }

    private fun setBgIndicator() {
        if (useBlackBackground(this)) {
            blackBgIndicator.visibility = View.VISIBLE
            darkBgIndicator.visibility = View.GONE
        } else {
            darkBgIndicator.visibility = View.VISIBLE
            blackBgIndicator.visibility = View.GONE
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
        val dialogView = View.inflate(this, R.layout.background_alert_dialog, null)
        val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.continueBtn.setOnClickListener {
            setUseBlackBackground(this, view.id == R.id.blackBgCircle)
            setBgIndicator()
            dialog.dismiss()
        }

        dialogView.backBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun cancelBtnClick(view: View) {
        finish()
    }

}
