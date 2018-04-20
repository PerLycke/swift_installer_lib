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

        custom_dark_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("202021"))
        }
        custom_black_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("000000"))
        }
        custom_style_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("202833"))
        }
        custom_nature_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("1C3B3A"))
        }
        custom_ocean_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("173145"))
        }
        custom_night_bg.setOnClickListener {
            updateColor(accentColor, Utils.convertToColorInt("363844"))
        }

        customize_confirm_btn.setOnClickListener {
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

        accent_hex_input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count >= 3 && Integer.toHexString(accentColor).substring(2) != s.toString())
                    updateColor(Utils.convertToColorInt(s.toString()), backgroundColor)
            }
        })

        hex_input_bg.addTextChangedListener(object : TextWatcher {
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
            if (compoundButton.id == R.id.material_theme) {
                material_theme.isChecked = b
                flat_theme.isChecked = !b
            } else {
                material_theme.isChecked = !b
                flat_theme.isChecked = b
            }
            setUseBackgroundPalette(this, material_theme.isChecked)
            updateColor(accentColor, backgroundColor)
        }
        material_theme.setOnCheckedChangeListener(listener)
        flat_theme.setOnCheckedChangeListener(listener)
    }

    override fun onResume() {
        super.onResume()
        if (finish) finish()
    }

    private fun setupAccentSheet() {
        palette.adapter = PaletteAdapter(resources.getIntArray(R.array.accent_colors))
        settingsIcons[0] = connections_icon
        settingsIcons[1] = sound_icon
        settingsIcons[2] = notifications_icon
    }

    private fun setBgIndicator() {
        custom_dark_bg_indicator.visibility = if (backgroundColor == Utils.convertToColorInt("202021")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_black_bg_indicator.visibility = if(backgroundColor == Utils.convertToColorInt("000000")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_style_bg_indicator.visibility = if (backgroundColor == Utils.convertToColorInt("202833")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_nature_bg_indicator.visibility = if (backgroundColor == Utils.convertToColorInt("1C3B3A")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_ocean_bg_indicator.visibility = if (backgroundColor == Utils.convertToColorInt("173145")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_night_bg_indicator.visibility = if (backgroundColor == Utils.convertToColorInt("363844")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        val back = settings_preview.drawable as LayerDrawable
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
        accent_hex_input.background.setTint(accentColor)
        accent_hex_input.setText(Integer.toHexString(accentColor).substring(2), TextView.BufferType.EDITABLE)
        hex_input_bg.setText(Integer.toHexString(backgroundColor).substring(2), TextView.BufferType.EDITABLE)

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
