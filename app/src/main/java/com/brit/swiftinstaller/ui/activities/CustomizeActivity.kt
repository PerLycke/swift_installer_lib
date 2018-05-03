package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
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
import kotlinx.android.synthetic.main.activity_customize.*
import kotlinx.android.synthetic.main.customize_accent.*
import kotlinx.android.synthetic.main.customize_background.*
import kotlinx.android.synthetic.main.customize_icons.*
import kotlinx.android.synthetic.main.customize_notifications.*
import kotlinx.android.synthetic.main.customize_preview_settings.*
import kotlinx.android.synthetic.main.customize_preview_sysui.*
import kotlinx.android.synthetic.main.toolbar_customize.*

class CustomizeActivity : ThemeActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(3)
    private var systemUiIcons: Array<ImageView?> = arrayOfNulls(6)

    private var accentColor = 0
    private var backgroundColor = 0

    private var finish = false
    private var usePalette = false
    private var useAospIcons = false
    private var notifShadow = false
    private var darkNotif = false

    private lateinit var materialPalette: MaterialPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_customize)
        setupAccentSheet()
        usePalette = useBackgroundPalette(this)
        useAospIcons = com.brit.swiftinstaller.utils.useAospIcons(this)
        notifShadow = useSenderNameFix(this)
        darkNotif = useDarkNotifBg(this)
        updateColor(getAccentColor(this), getBackgroundColor(this), true, false)
        updateIcons()

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
            val oldShadow = useSenderNameFix(this)
            val oldNotifbg = useDarkNotifBg(this)

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
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    if (!apps.contains("android"))
                        apps.add("android")
                }
            }

            if (darkNotif != oldNotifbg) {
                setUseDarkNotifBg(this, darkNotif)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    if (!apps.contains("android"))
                        apps.add("android")
                }
            }

            if (notifShadow != oldShadow) {
                setUseSenderNameFix(this, notifShadow)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    if (!apps.contains("android"))
                        apps.add("android")
                }
            }

            if (useAospIcons != oldIcons) {
                setUseAospIcons(this, useAospIcons)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("com.samsung.android.lool")
                    apps.add("com.samsung.android.themestore")
                    apps.add("com.android.settings")
                    apps.add("com.android.systemui")
                }
            }

            if (recompile && apps.isNotEmpty()) {

                val receiver = object : BroadcastReceiver() {
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
                val launch = getSharedPreferences("launched", Context.MODE_PRIVATE).getString("launched","first")
                val thisLaunch = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("thisLaunched", false)
                intent.putStringArrayListExtra("apps", apps)


                if (launch == "default" && thisLaunch) {
                    startActivity(intent)
                } else {
                    if (launch == "second") {
                        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                        themeDialog()
                        builder.setTitle(R.string.reboot_delay_title)
                        builder.setMessage(R.string.reboot_delay_msg)
                        builder.setPositiveButton(R.string.proceed, { dialogInterface, _ ->
                            getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "default").apply()
                            dialogInterface.dismiss()
                            startActivity(intent)
                        })
                        builder.setNegativeButton(R.string.cancel, { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        })

                        val dialog = builder.create()
                        dialog.show()
                    } else {
                        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                        themeDialog()
                        builder.setTitle(R.string.installing_and_uninstalling_title)
                        builder.setMessage(R.string.installing_and_uninstalling_msg)
                        builder.setPositiveButton(R.string.proceed, { dialogInterface, _ ->
                            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("thisLaunched", true).apply()
                            dialogInterface.dismiss()
                            startActivity(intent)
                        })

                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            } else {
                finish()
            }
        }

        accent_hex_input.addTextChangedListener(object : TextWatcher {
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

        material_theme.isChecked = usePalette
        flat_theme.isChecked = !usePalette

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

        white_notifications.isChecked = !darkNotif
        dark_notifications.isChecked = darkNotif

        val notifBglistener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.id == R.id.dark_notifications) {
                dark_notifications.isChecked = b
                white_notifications.isChecked = !b
                shadowFixLayout.visibility = View.VISIBLE
                customizations_scrollview.postDelayed({ customizations_scrollview.fullScroll(ScrollView.FOCUS_DOWN) }, 200)
            } else {
                dark_notifications.isChecked = !b
                white_notifications.isChecked = b
                shadowFixLayout.visibility = View.GONE
            }
            darkNotif = dark_notifications.isChecked
            updateColor(accentColor, backgroundColor, false, true)
        }

        dark_notifications.setOnCheckedChangeListener(notifBglistener)
        white_notifications.setOnCheckedChangeListener(notifBglistener)

        shadow_disabled.isChecked = !notifShadow
        shadow_enabled.isChecked = notifShadow

        val notifListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.id == R.id.shadow_disabled) {
                shadow_disabled.isChecked = isChecked
                shadow_enabled.isChecked = !isChecked
            } else {
                shadow_disabled.isChecked = !isChecked
                shadow_enabled.isChecked = isChecked
            }
            notifShadow = shadow_enabled.isChecked
            updateColor(accentColor, backgroundColor, false, true)
        }
        shadow_disabled.setOnCheckedChangeListener(notifListener)
        shadow_enabled.setOnCheckedChangeListener(notifListener)

        aosp_icons.isChecked = useAospIcons
        stock_icons.isChecked = !useAospIcons

        val iconListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.id == R.id.aosp_icons) {
                aosp_icons.isChecked = isChecked
                stock_icons.isChecked = !isChecked
            } else {
                aosp_icons.isChecked = !isChecked
                stock_icons.isChecked = isChecked
            }
            useAospIcons = aosp_icons.isChecked
            updateIcons()
        }
        aosp_icons.setOnCheckedChangeListener(iconListener)
        stock_icons.setOnCheckedChangeListener(iconListener)
    }

    override fun onResume() {
        super.onResume()
        if (finish) finish()
    }

    private fun setupAccentSheet() {
        palette.adapter = PaletteAdapter(resources.getIntArray(R.array.accent_colors))
        settingsIcons[0] = settings_connections_icon
        settingsIcons[1] = settings_sound_icon
        settingsIcons[2] = settings_notifications_icon
        systemUiIcons[0] = systemui_wifi_icon
        systemUiIcons[1] = systemui_airplane_icon
        systemUiIcons[2] = systemui_bluetooth_icon
        systemUiIcons[3] = systemui_flashlight_icon
        systemUiIcons[4] = systemui_sound_icon
        systemUiIcons[5] = systemui_rotation_icon

        Log.d("TEST", "name - ${resources.getResourceEntryName(R.id.settings_connections_icon)}")
    }

    private fun setBgIndicator() {
        custom_dark_bg_indicator.visibility = if (backgroundColor == convertToColorInt("202026")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        custom_black_bg_indicator.visibility = if (backgroundColor == convertToColorInt("000000")) {
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
        val searchFrameBackground = searchbar_bg.drawable as LayerDrawable
        //back.setTintMode(PorterDuff.Mode.SRC_ATOP)
        settingsBackground.findDrawableByLayerId(R.id.preview_background).setTint(materialPalette.backgroundColor)
        systemUIBackground.findDrawableByLayerId(R.id.preview_background).setTint(materialPalette.backgroundColor)
        searchFrameBackground.findDrawableByLayerId(R.id.search_frame_background).setTint(materialPalette.cardBackgroud)
    }

    private fun updateIcons() {
        for (icon in settingsIcons) {
            if (icon != null) {
                val idName = "ic_${resources.getResourceEntryName(icon.id)}_${if (useAospIcons) {
                    "aosp"
                } else {
                    "stock"
                }}"
                val id = resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(getDrawable(id))
                }
            }
        }
        for (icon in systemUiIcons) {
            if (icon != null) {
                val idName = "ic_${resources.getResourceEntryName(icon.id)}_${if (useAospIcons) {
                    "aosp"
                } else {
                    "stock"
                }}"
                val id = resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(getDrawable(id))
                }
            }
        }
    }

    fun updateColor(accentColor: Int, backgroundColor: Int, updateHex: Boolean, force: Boolean) {
        Log.d("TEST", "accent - ${Integer.toHexString(accentColor)}")
        Log.d("TEST", "background - ${Integer.toHexString(backgroundColor)}")
        updateColors(backgroundColor, usePalette)
        if (force || this.accentColor != accentColor) {
            this.accentColor = accentColor
            for (icon: ImageView? in settingsIcons) {
                icon?.setColorFilter(accentColor)
            }
            for (icon: ImageView? in systemUiIcons) {
                icon?.setColorFilter(accentColor)
            }
            accent_hex_input.background.setTint(accentColor)
            hex_input_bg.background.setTint(accentColor)
            if (updateHex && accent_hex_input.text.toString() != Integer.toHexString(accentColor).substring(2))
                accent_hex_input.setText(Integer.toHexString(accentColor).substring(2), TextView.BufferType.EDITABLE)

            custom_dark_bg_indicator.drawable.setTint(accentColor)
            custom_black_bg_indicator.drawable.setTint(accentColor)
            custom_style_bg_indicator.drawable.setTint(accentColor)
            custom_nature_bg_indicator.drawable.setTint(accentColor)
            custom_ocean_bg_indicator.drawable.setTint(accentColor)
            custom_night_bg_indicator.drawable.setTint(accentColor)

            val buttonColor = ColorStateList(
                    arrayOf(
                            intArrayOf(-android.R.attr.state_checked), //disabled
                            intArrayOf(android.R.attr.state_checked) //enabled
                    ),
                    intArrayOf(
                            ContextCompat.getColor(this, R.color.radio_button_disabled)
                            , accentColor
                    )
            )

            material_theme.buttonTintList = buttonColor
            flat_theme.buttonTintList = buttonColor
            stock_icons.buttonTintList = buttonColor
            aosp_icons.buttonTintList = buttonColor
            white_notifications.buttonTintList = buttonColor
            dark_notifications.buttonTintList = buttonColor
            shadow_enabled.buttonTintList = buttonColor
            shadow_disabled.buttonTintList = buttonColor
        }
        if (force || this.backgroundColor != backgroundColor) {
            materialPalette = MaterialPalette.createPalette(backgroundColor, usePalette)
            Log.d("TEST", "MaterialPalette : $materialPalette")
            this.backgroundColor = backgroundColor
            if (updateHex && hex_input_bg.text.toString() != Integer.toHexString(backgroundColor).substring(2))
                hex_input_bg.setText(Integer.toHexString(backgroundColor).substring(2), TextView.BufferType.EDITABLE)
            setBgIndicator()
        }

        if (notifShadow) {
            preview_sysui_sender.text = getString(R.string.dark_notifications)
            preview_sysui_msg.text = getString(R.string.dark_notifications_preview)
            preview_sysui_msg.setText(R.string.notification_fix_summary)
            preview_sysui_app_title.setShadowLayer(2.0f, 0.0f, 0.0f, Color.WHITE)
            preview_sysui_sender.setTextColor(Color.BLACK)
            preview_sysui_sender.setShadowLayer(2.0f, 0.0f, 0.0f, Color.WHITE)
        } else {
            preview_sysui_sender.setText(R.string.dark_notifications)
            preview_sysui_msg.text = getString(R.string.dark_notifications_preview_normal)
            preview_sysui_app_title.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
            preview_sysui_sender.setTextColor(Color.WHITE)
            preview_sysui_sender.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
        }

        if (darkNotif) {
            notif_bg_layout.drawable.setTint(Color.TRANSPARENT)
            if (notifShadow) {
                preview_sysui_sender.setTextColor(Color.BLACK)
            } else {
                preview_sysui_sender.setTextColor(Color.WHITE)
            }
            preview_sysui_msg.setTextColor(Color.parseColor("#b3ffffff"))
            shadowFixLayout.visibility = View.VISIBLE
        } else {
            preview_sysui_sender.text = getString(R.string.white_notifications)
            preview_sysui_msg.text = getString(R.string.white_notifications_preview)
            notif_bg_layout.drawable.setTint(Color.parseColor("#f5f5f5"))
            preview_sysui_sender.setTextColor(Color.BLACK)
            preview_sysui_msg.setTextColor(Color.parseColor("#8a000000"))
            shadowFixLayout.visibility = View.GONE
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
