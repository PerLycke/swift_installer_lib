package com.brit.swiftinstaller.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.ColorUtils.checkAccentColor
import com.brit.swiftinstaller.utils.ColorUtils.checkBackgroundColor
import com.brit.swiftinstaller.utils.ColorUtils.convertToColorInt
import kotlinx.android.synthetic.main.customize_accent.*
import kotlinx.android.synthetic.main.customize_background.*
import kotlinx.android.synthetic.main.customize_preview_settings.*
import kotlinx.android.synthetic.main.toolbar_customize.*
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import kotlinx.android.synthetic.main.customize_preview_sysui.*

class CustomizeActivity : ThemeActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(9)

    private var accentColor = 0
    private var backgroundColor = 0

    private var finish = false
    private var usePalette = false
    private var useAospIcons = false

    private lateinit var materialPalette: MaterialPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_customize)
        setupAccentSheet()
        usePalette = useBackgroundPalette(this)
        updateColor(getAccentColor(this), getBackgroundColor(this), true, false)

        custom_dark_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("202026"), true, false)
        }
        custom_black_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("000000"), true, false)
        }
        custom_style_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("202833"), true, false)
        }
        custom_nature_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("1C3B3A"), true, false)
        }
        custom_ocean_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("173145"), true, false)
        }
        custom_night_bg.setOnClickListener {
            updateColor(accentColor, convertToColorInt("363844"), true, false)
        }

        customize_confirm_btn.setOnClickListener {
            var recompile = false
            val apps = ArrayList<String>()

            val oldAccent = getAccentColor(this)
            val oldBackground = getBackgroundColor(this)
            val oldPalette = useBackgroundPalette(this)
            val oldIcons = useAospIcons(this)

            if (oldAccent != accentColor) {
                setAccentColor(this, accentColor)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("android")
                }
            }

            if (oldBackground != backgroundColor) {
                setBackgroundColor(this, backgroundColor)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    if (!apps.contains("android"))
                        apps.add("android")
                }
            }

            if (usePalette != oldPalette) {
                setUseBackgroundPalette(this, usePalette)
                recompile = true
                if (!apps.contains("android"))
                    apps.add("android")
            }

            if (useAospIcons != oldIcons) {
                setUseAospIcons(this, useAospIcons)
                recompile = true
                apps.add("com.samsung.android.lool")
                apps.add("com.samsung.android.themestore")
                apps.add("com.android.settings")
                apps.add("com.android.systemui")
            }

            if (recompile && apps.isNotEmpty()) {

                val receiver = object: BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (intent.action == InstallSummaryActivity.ACTION_INSTALL_CANCELLED) {
                            if (oldAccent != getAccentColor(context)) {
                                setAccentColor(context, oldAccent)
                            }
                            if (oldBackground != getBackgroundColor(context)) {
                                setBackgroundColor(context, oldBackground)
                            }
                            if (oldPalette != useBackgroundPalette(context)) {
                                setUseBackgroundPalette(context, oldPalette)
                            }
                            if (oldIcons != com.brit.swiftinstaller.utils.useAospIcons(context)) {
                                setUseAospIcons(context, oldIcons)
                            }
                            LocalBroadcastManager.getInstance(context.applicationContext)
                                    .unregisterReceiver(this)
                        }
                    }
                }
                LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver,
                        IntentFilter(InstallSummaryActivity.ACTION_INSTALL_CANCELLED))

                finish = true
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
                if (s?.length == 3 || s?.length == 6) {
                    if (checkAccentColor(convertToColorInt(s.toString()))) {
                        updateColor(convertToColorInt(s.toString()), backgroundColor, false, false)
                    } else {
                        Toast.makeText(this@CustomizeActivity, R.string.invalid_accent,
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        hex_input_bg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 3 || s?.length == 6) {
                    if (checkBackgroundColor(convertToColorInt(s.toString()))) {
                        updateColor(accentColor, convertToColorInt(s.toString()), false, false)
                    } else {
                        Toast.makeText(this@CustomizeActivity,
                                R.string.invalid_background, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        val viewpager: ViewPager = findViewById(R.id.preview_pager)
        viewpager.pageMargin = 64
        val adapter = PreviewPagerAdapter()
        viewpager.adapter = adapter

        if (usePalette) {
            material_theme.isChecked = true
            flat_theme.isChecked = false
        } else {
            material_theme.isChecked = false
            flat_theme.isChecked = true
        }

        val listener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.id == R.id.material_theme) {
                material_theme.isChecked = b
                flat_theme.isChecked = !b
            } else {
                material_theme.isChecked = !b
                flat_theme.isChecked = b
            }
            usePalette = material_theme.isChecked
            updateColor(accentColor, backgroundColor, false, true)
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
        settingsIcons[3] = preview_sysui_icon1
        settingsIcons[4] = preview_sysui_icon2
        settingsIcons[5] = preview_sysui_icon3
        settingsIcons[6] = preview_sysui_icon4
        settingsIcons[7] = preview_sysui_icon5
        settingsIcons[8] = preview_sysui_icon6
    }

    private fun setBgIndicator() {
        custom_dark_bg_indicator.visibility = if (backgroundColor == convertToColorInt("202026")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_black_bg_indicator.visibility = if(backgroundColor == convertToColorInt("000000")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_style_bg_indicator.visibility = if (backgroundColor == convertToColorInt("202833")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_nature_bg_indicator.visibility = if (backgroundColor == convertToColorInt("1C3B3A")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_ocean_bg_indicator.visibility = if (backgroundColor == convertToColorInt("173145")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_night_bg_indicator.visibility = if (backgroundColor == convertToColorInt("363844")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        val settingsBackground = settings_preview?.drawable as LayerDrawable
        val systemUIBackground = preview_sysui_bg.drawable as LayerDrawable
        //back.setTintMode(PorterDuff.Mode.SRC_ATOP)
        settingsBackground.findDrawableByLayerId(R.id.preview_background).setTint(materialPalette.backgroundColor)
        systemUIBackground.findDrawableByLayerId(R.id.preview_background).setTint(materialPalette.backgroundColor)

    }

    fun updateColor(accentColor: Int, backgroundColor: Int, updateHex: Boolean, force: Boolean) {
        Log.d("TEST", "accent - ${Integer.toHexString(accentColor)}")
        Log.d("TEST", "background - ${Integer.toHexString(backgroundColor)}")
        updateColors(accentColor, backgroundColor, usePalette)
        if (force || this.accentColor != accentColor) {
            this.accentColor = accentColor
            for (icon: ImageView? in settingsIcons) {
                icon?.setColorFilter(accentColor)
            }
            accent_hex_input.background.setTint(accentColor)
            hex_input_bg.background.setTint(accentColor)
            if (updateHex && accent_hex_input.text.toString() != Integer.toHexString(accentColor).substring(2))
                accent_hex_input.setText(Integer.toHexString(accentColor).substring(2), TextView.BufferType.EDITABLE)
        }
        if (force || this.backgroundColor != backgroundColor) {
            materialPalette = MaterialPalette.createPalette(backgroundColor, usePalette)
            Log.d("TEST", "MaterialPalette : $materialPalette")
            this.backgroundColor = backgroundColor
            if (updateHex && hex_input_bg.text.toString() != Integer.toHexString(backgroundColor).substring(2))
                hex_input_bg.setText(Integer.toHexString(backgroundColor).substring(2), TextView.BufferType.EDITABLE)
            setBgIndicator()
        }
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
                updateColor(mColors[position], backgroundColor, true, false)
            }
            return mainView
        }
    }

    fun cancelBtnClick(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    inner class PreviewPagerAdapter : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            return if (position == 0) {
                findViewById(R.id.settings_preview_page)
            } else {
                findViewById(R.id.systemui_preview_page)
            }
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return 2
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

    }
}
