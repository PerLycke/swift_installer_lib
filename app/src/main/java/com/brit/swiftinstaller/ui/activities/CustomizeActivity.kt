package com.brit.swiftinstaller.ui.activities

import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable
import com.brit.swiftinstaller.utils.*
import kotlinx.android.synthetic.main.customize_accent.*
import kotlinx.android.synthetic.main.customize_background.*
import kotlinx.android.synthetic.main.customize_preview.*
import kotlinx.android.synthetic.main.toolbar_customize.*

class CustomizeActivity : AppCompatActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(3)

    private var accentColor = 0
    private var backgroundColor = 0

    private var finish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accentColor = getAccentColor(this)
        backgroundColor = getBackgroundColor(this)

        setContentView(R.layout.activity_customize)
        setupAccentSheet()
        updateColor(accentColor, backgroundColor)

        customDarkBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("202021"))
        }
        customBlackBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("000000"))
        }
        customStyleBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("202833"))
        }
        customNatureBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("1C3B3A"))
        }
        customOceanBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("173145"))
        }
        customNightBg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("363844"))
        }

        customizeConfirmBtn.setOnClickListener {
            var recompile = false
            if (getAccentColor(this) != accentColor) {
                setAccentColor(this, accentColor)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                }
            }

            if (getBackgroundColor(this) != backgroundColor) {
                setBackgroundColor(this, backgroundColor)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                }
            }

            if (recompile) {
                finish = true
                val apps = ArrayList<String>()
                apps.add("android")
                val intent = Intent(this, InstallActivity::class.java)
                intent.putStringArrayListExtra("apps", apps)
                startActivity(intent)
            } else {
                finish()
            }
        }

        accentHexInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count >= 3 && Integer.toHexString(accentColor).substring(2) != s.toString())
                    updateColor(Utils.convertToColorInt(s.toString()), backgroundColor)
            }
        })

        hexInputBg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count >= 3 && (Integer.toHexString(backgroundColor).substring(2) != s.toString())) {
                    updateColor(accentColor, Utils.convertToColorInt(s.toString()))
                }
            }
        })

        val listener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.id == R.id.materialBackground) {
                materialBackground.isChecked = b
                flatBackground.isChecked = !b
            } else {
                materialBackground.isChecked = !b
                flatBackground.isChecked = b
            }
            setUseBackgroundPalette(this, materialBackground.isChecked)
            updateColor(accentColor, backgroundColor)
        }
        materialBackground.setOnCheckedChangeListener(listener)
        flatBackground.setOnCheckedChangeListener(listener)
    }

    override fun onResume() {
        super.onResume()
        if (finish) finish()
    }

    private fun setupAccentSheet() {
        palette.adapter = PaletteAdapter(resources.getIntArray(R.array.accent_colors))
        settingsIcons[0] = connectionsIcon
        settingsIcons[1] = soundIcon
        settingsIcons[2] = notificationsIcon
    }

    private fun setBgIndicator() {
        customDarkBgIndicator.visibility = if (backgroundColor == Utils.convertToColorInt("202021")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        customBlackBgIndicator.visibility = if(backgroundColor == Utils.convertToColorInt("000000")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        customStyleBgIndicator.visibility = if (backgroundColor == Utils.convertToColorInt("202833")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        customNatureBgIndicator.visibility = if (backgroundColor == Utils.convertToColorInt("1C3B3A")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        customOceanBgIndicator.visibility = if (backgroundColor == Utils.convertToColorInt("173145")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        customNightBgIndicator.visibility = if (backgroundColor == Utils.convertToColorInt("363844")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        val back = settingsPreview.drawable as LayerDrawable
        //back.setTintMode(PorterDuff.Mode.SRC_ATOP)
        back.findDrawableByLayerId(R.id.settings_preview_background).setTint(backgroundColor)
        if (useBackgroundPalette(this)) {
            back.findDrawableByLayerId(R.id.settings_preview_frame).setTint(backgroundColor - 0xf7f7f8)
        } else {
            back.findDrawableByLayerId(R.id.settings_preview_frame).setTint(backgroundColor)
        }
    }

    fun updateColor(accentColor: Int, backgroundColor: Int) {
        this.accentColor = accentColor
        this.backgroundColor = backgroundColor
        for (icon: ImageView? in settingsIcons) {
            icon!!.setColorFilter(accentColor)
        }
        accentHexInput.background.setTint(accentColor)
        accentHexInput.setText(Integer.toHexString(accentColor).substring(2), TextView.BufferType.EDITABLE)
        hexInputBg.setText(Integer.toHexString(backgroundColor).substring(2), TextView.BufferType.EDITABLE)

        setBgIndicator()
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
                updateColor(mColors[position], backgroundColor)
            }
            return mainView
        }
    }

    fun cancelBtnClick(view: View) {
        finish()
    }
}
