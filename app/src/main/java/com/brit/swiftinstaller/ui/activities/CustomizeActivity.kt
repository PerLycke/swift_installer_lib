package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
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
import kotlinx.android.synthetic.main.customize_clock.*
import kotlinx.android.synthetic.main.customize_icons.*
import kotlinx.android.synthetic.main.customize_notifications.*
import kotlinx.android.synthetic.main.customize_preview_settings.*
import kotlinx.android.synthetic.main.customize_preview_sysui.*
import kotlinx.android.synthetic.main.customize_systemui.*
import kotlinx.android.synthetic.main.fab_sheet_personalize.view.*

class CustomizeActivity : ThemeActivity() {

    private var settingsIcons: Array<ImageView?> = arrayOfNulls(3)
    private var systemUiIcons: Array<ImageView?> = arrayOfNulls(6)

    private var accentColor = 0
    private var backgroundColor = 0

    private var finish = false
    private var usePalette = false
    private var useAospIcons = false
    private var useStockAccentIcons = false
    private var useStockMultiIcons = false
    private var usePIcons = false
    private var notifShadow = false
    private var darkNotif = false
    private var useRightClock = false
    private var useLeftClock = false
    private var useCenteredClock = false
    private var usePStyle = false

    private lateinit var materialPalette: MaterialPalette
    private val handler = Handler()
    private var parentActivity: String? = "parent"
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = intent.getStringExtra("parentActivity")

        setContentView(R.layout.activity_customize)
        handler.post {
            setupAccentSheet()
            usePalette = useBackgroundPalette(this)
            useAospIcons = com.brit.swiftinstaller.utils.useAospIcons(this)
            useStockAccentIcons = com.brit.swiftinstaller.utils.useStockAccentIcons(this)
            useStockMultiIcons = com.brit.swiftinstaller.utils.useStockMultiIcons(this)
            usePIcons = com.brit.swiftinstaller.utils.usePIcons(this)
            notifShadow = useSenderNameFix(this)
            darkNotif = useDarkNotifBg(this)
            useRightClock = com.brit.swiftinstaller.utils.useRightClock(this)
            useLeftClock = com.brit.swiftinstaller.utils.useLeftClock(this)
            useCenteredClock = com.brit.swiftinstaller.utils.useCenteredClock(this)
            usePStyle = com.brit.swiftinstaller.utils.usePstyle(this)
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

            setupHexInputs()
            setupThemeOptions()

            val viewpager: ViewPager = findViewById(R.id.preview_pager)
            viewpager.pageMargin = 64
            val adapter = PreviewPagerAdapter()
            viewpager.adapter = adapter

            personalize_fab.setOnClickListener {
                personalizeFabClick()
            }

            baseThemeInfo.setOnClickListener {
                val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                val dialogBg = getDrawable(R.drawable.dialog_bg) as LayerDrawable
                dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(backgroundColor)
                builder.setTitle(getString(R.string.base_theme_dialog_title))
                builder.setMessage(getString(R.string.base_theme_dialog_info))
                builder.setPositiveButton(R.string.ok, { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                val dialog = builder.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accentColor)
            }
        }
    }

    private fun showFab() {
        handler.postDelayed({
            if (personalize_fab.visibility == View.GONE) {
                personalize_fab.visibility = View.VISIBLE
                personalize_fab.startAnimation(AnimationUtils.loadAnimation(this@CustomizeActivity, android.R.anim.fade_in))
            }
            if (accent_hex_input.hasFocus() || hex_input_bg.hasFocus()) {
                personalize_fab.setImageDrawable(ContextCompat.getDrawable(this@CustomizeActivity, R.drawable.ic_done))
            } else {
                personalize_fab.setImageDrawable(ContextCompat.getDrawable(this@CustomizeActivity, R.drawable.ic_fab_install))
            }
        }, 300)
    }

    private fun hideFab() {
        if (personalize_fab.visibility == View.VISIBLE) {
            personalize_fab.visibility = View.GONE
        }
    }

    private fun setupHexInputs() {
        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideFab()
                showFab()
            } else {
                hideFab()
                showFab()
            }
        }

        accent_hex_input.onFocusChangeListener = onFocusChangeListener
        hex_input_bg.onFocusChangeListener = onFocusChangeListener
        accent_hex_input.setOnKeyListener({ v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> accent_hex_input.clearFocus()
                }
            }
            false
        })
        hex_input_bg.setOnKeyListener({ v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> hex_input_bg.clearFocus()
                }
            }
            false
        })

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
    }

    private fun setupThemeOptions() {

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

        when {
            useAospIcons -> {
                aosp_icons.isChecked = true
            }
            useStockMultiIcons -> {
                stock_icons_multi.isChecked = true
            }
            usePIcons -> {
                p_icons.isChecked = true
            }
            else -> {
                stock_icons.isChecked = true
            }
        }

        val iconListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when {
                buttonView.id == R.id.aosp_icons -> {
                    if (aosp_icons.isChecked) {
                        aosp_icons.isChecked = true
                        stock_icons.isChecked = false
                        stock_icons_multi.isChecked = false
                        p_icons.isChecked = false
                        for (icon: ImageView? in settingsIcons) {
                            icon?.setColorFilter(accentColor)
                        }
                        useAospIcons = true
                        updateIcons()
                    } else {
                        useAospIcons = false
                    }
                }
                buttonView.id == R.id.stock_icons -> {
                    if (stock_icons.isChecked) {
                        aosp_icons.isChecked = false
                        stock_icons.isChecked = true
                        stock_icons_multi.isChecked = false
                        p_icons.isChecked = false
                        for (icon: ImageView? in settingsIcons) {
                            icon?.setColorFilter(accentColor)
                        }
                        useStockAccentIcons = true
                        updateIcons()
                    } else {
                        useStockAccentIcons = false
                    }
                }
                buttonView.id == R.id.stock_icons_multi -> {
                    if (stock_icons_multi.isChecked) {
                        aosp_icons.isChecked = false
                        stock_icons.isChecked = false
                        stock_icons_multi.isChecked = true
                        p_icons.isChecked = false
                        for (icon: ImageView? in settingsIcons) {
                            icon?.clearColorFilter()
                        }
                        useStockMultiIcons = true
                        updateIcons()
                    } else {
                        useStockMultiIcons = false
                    }
                }
                buttonView.id == R.id.p_icons -> {
                    if (p_icons.isChecked) {
                        aosp_icons.isChecked = false
                        stock_icons.isChecked = false
                        stock_icons_multi.isChecked = false
                        p_icons.isChecked = true
                        for (icon: ImageView? in settingsIcons) {
                            icon?.clearColorFilter()
                        }
                        usePIcons = true
                        updateIcons()
                    } else {
                        usePIcons = false
                    }
                }
            }
        }

        aosp_icons.setOnCheckedChangeListener(iconListener)
        stock_icons.setOnCheckedChangeListener(iconListener)
        stock_icons_multi.setOnCheckedChangeListener(iconListener)
        p_icons.setOnCheckedChangeListener(iconListener)

        when {
            useLeftClock -> {
                left_clock.isChecked = true
                if (clock_left.visibility == View.GONE) {
                    clock_left.visibility = View.VISIBLE
                }
            }
            useCenteredClock -> {
                centered_clock.isChecked = true
                if (clock_centered.visibility == View.GONE) {
                    clock_centered.visibility = View.VISIBLE
                }
            }
            else -> {
                right_clock.isChecked = true
                if (clock_right.visibility == View.GONE) {
                    clock_right.visibility = View.VISIBLE
                }
            }
        }

        val clockListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when {
                buttonView.id == R.id.right_clock -> {
                    if (right_clock.isChecked) {
                        right_clock.isChecked = true
                        left_clock.isChecked = false
                        centered_clock.isChecked = false
                        clock_right.visibility = View.VISIBLE
                        clock_left.visibility = View.GONE
                        clock_centered.visibility = View.GONE
                        useRightClock = true
                    } else {
                        useRightClock = false
                    }
                }
                buttonView.id == R.id.left_clock -> {
                    if (left_clock.isChecked) {
                        right_clock.isChecked = false
                        left_clock.isChecked = true
                        centered_clock.isChecked = false
                        clock_right.visibility = View.GONE
                        clock_left.visibility = View.VISIBLE
                        clock_centered.visibility = View.GONE
                        useLeftClock = true
                    } else {
                        useLeftClock = false
                    }
                }
                buttonView.id == R.id.centered_clock -> {
                    if (centered_clock.isChecked) {
                        right_clock.isChecked = false
                        left_clock.isChecked = false
                        centered_clock.isChecked = true
                        clock_right.visibility = View.GONE
                        clock_left.visibility = View.GONE
                        clock_centered.visibility = View.VISIBLE
                        useCenteredClock = true
                    } else {
                        useCenteredClock = false
                    }
                }
            }
        }

        right_clock.setOnCheckedChangeListener(clockListener)
        left_clock.setOnCheckedChangeListener(clockListener)
        centered_clock.setOnCheckedChangeListener(clockListener)

        default_style.isChecked = !usePStyle
        p_style.isChecked = usePStyle

        val styleListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.id == R.id.default_style) {
                default_style.isChecked = isChecked
                p_style.isChecked = !isChecked
            } else {
                default_style.isChecked = !isChecked
                p_style.isChecked = isChecked
            }
            usePStyle = p_style.isChecked
            updateColor(accentColor, backgroundColor, false, true)
        }
        p_style.setOnCheckedChangeListener(styleListener)
        default_style.setOnCheckedChangeListener(styleListener)

    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (parentActivity == "tutorial") {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateColors(backgroundColor, usePalette)
        if (finish) finish()
    }

    private fun checkAndAddApp(apps: ArrayList<String>, app: String) {
        if (!apps.contains(app)) {
            apps.add(app)
        }
    }

    private fun personalizeFabClick() {

        if (hex_input_bg.hasFocus() || accent_hex_input.hasFocus()) {
            val imm = getSystemService(InputMethodManager::class.java)
            imm.hideSoftInputFromWindow(accent_hex_input.windowToken, 0)
            if (accent_hex_input.hasFocus()) {
                accent_hex_input.clearFocus()
            } else {
                hex_input_bg.clearFocus()
            }
            return
        }

        bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.fab_sheet_personalize, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.window.decorView.findViewById<View>(R.id.design_bottom_sheet).setBackgroundColor(backgroundColor)

        if (parentActivity == "tutorial") {
            sheetView.personalization_confirm_txt.text = getString(R.string.continue_theming)
            sheetView.personalization_discard_txt.text = getString(R.string.exit_to_main)
        }
        bottomSheetDialog.show()

        sheetView.personalization_confirm_txt.setOnClickListener {
            bottomSheetDialog.dismiss()
            var recompile = false
            val apps = ArrayList<String>()

            val oldAccent = getAccentColor(this)
            val oldBackground = getBackgroundColor(this)
            val oldPalette = useBackgroundPalette(this)
            val oldIcons = useAospIcons(this)
            val oldStockAccentIcons = useStockAccentIcons(this)
            val oldStockMultiIcons = useStockMultiIcons(this)
            val oldAndroidPIcons = usePIcons(this)
            val oldShadow = useSenderNameFix(this)
            val oldNotifbg = useDarkNotifBg(this)
            val oldRightClock = useRightClock(this)
            val oldLeftClock = useLeftClock(this)
            val oldCenteredClock = useCenteredClock(this)
            val oldPStyle = usePstyle(this)

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
                    checkAndAddApp(apps, "android")
                }
            }

            if (usePalette != oldPalette) {
                setUseBackgroundPalette(this, usePalette)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps, "android")
                }
            }

            if (darkNotif != oldNotifbg) {
                setUseDarkNotifBg(this, darkNotif)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps, "android")
                }
            }

            if (notifShadow != oldShadow) {
                setUseSenderNameFix(this, notifShadow)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps, "android")
                }
            }

            if (useAospIcons != oldIcons && useAospIcons) {
                setUseAospIcons(this, true)
                setUseStockAccentIcons(this, false)
                setUseStockMultiIcons(this, false)
                setUsePIcons(this, false)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("com.samsung.android.lool")
                    apps.add("com.samsung.android.themestore")
                    apps.add("com.android.settings")
                    apps.add("com.android.systemui")
                }
            }

            if (useStockAccentIcons != oldStockAccentIcons && useStockAccentIcons) {
                setUseAospIcons(this, false)
                setUseStockAccentIcons(this, true)
                setUseStockMultiIcons(this, false)
                setUsePIcons(this, false)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("com.samsung.android.lool")
                    apps.add("com.samsung.android.themestore")
                    apps.add("com.android.settings")
                    apps.add("com.android.systemui")
                }
            }

            if (useStockMultiIcons != oldStockMultiIcons && useStockMultiIcons) {
                setUseAospIcons(this, false)
                setUseStockAccentIcons(this, false)
                setUseStockMultiIcons(this, true)
                setUsePIcons(this, false)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("com.samsung.android.lool")
                    apps.add("com.samsung.android.themestore")
                    apps.add("com.android.settings")
                    apps.add("com.android.systemui")
                }
            }

            if (usePIcons != oldAndroidPIcons && usePIcons) {
                setUseStockMultiIcons(this, false)
                setUseAospIcons(this, false)
                setUseStockAccentIcons(this, false)
                setUsePIcons(this, true)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    apps.add("com.samsung.android.lool")
                    apps.add("com.samsung.android.themestore")
                    apps.add("com.android.settings")
                    apps.add("com.android.systemui")
                }
            }

            if (useRightClock != oldRightClock && useRightClock) {
                setUseLeftClock(this, false)
                setUseCenteredClock(this, false)
                setUseRightClock(this, true)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps,"com.android.systemui")
                }
            }

            if (useLeftClock != oldLeftClock && useLeftClock) {
                setUseCenteredClock(this, false)
                setUseRightClock(this, false)
                setUseLeftClock(this, true)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps,"com.android.systemui")
                }
            }

            if (useCenteredClock != oldCenteredClock && useCenteredClock) {
                setUseLeftClock(this, false)
                setUseRightClock(this, false)
                setUseCenteredClock(this, true)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps,"com.android.systemui")
                }
            }

            if (usePStyle != oldPStyle && usePStyle) {
                setUsePStyle(this, true)
                if (Utils.isOverlayInstalled(this, Utils.getOverlayPackageName("android"))) {
                    recompile = true
                    checkAndAddApp(apps, "com.android.systemui")
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
                            if (oldStockAccentIcons != com.brit.swiftinstaller.utils.useStockAccentIcons(context)) {
                                setUseStockAccentIcons(context, oldStockAccentIcons)
                            }
                            if (oldStockMultiIcons != com.brit.swiftinstaller.utils.useStockMultiIcons(context)) {
                                setUseStockMultiIcons(context, oldStockMultiIcons)
                            }
                            if (oldAndroidPIcons != com.brit.swiftinstaller.utils.usePIcons(context)) {
                                setUsePIcons(context, oldAndroidPIcons)
                            }
                            if (oldRightClock != com.brit.swiftinstaller.utils.useRightClock(context)) {
                                setUseRightClock(context, oldRightClock)
                            }
                            if (oldLeftClock != com.brit.swiftinstaller.utils.useLeftClock(context)) {
                                setUseLeftClock(context, oldLeftClock)
                            }
                            if (oldCenteredClock != com.brit.swiftinstaller.utils.useCenteredClock(context)) {
                                setUseCenteredClock(context, oldCenteredClock)
                            }
                            if (oldPStyle != com.brit.swiftinstaller.utils.usePstyle(context)) {
                                setUsePStyle(context, oldPStyle)
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
                    when (launch) {
                        "first" -> {
                            getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "second").apply()
                            startActivity(intent)
                        }
                        "second" -> {
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
                        }
                        else -> {
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
                }
            } else {
                if (parentActivity == "tutorial") {
                    startActivity(Intent(this, OverlaysActivity::class.java))
                } else {
                    finish()
                }
            }
        }

        sheetView.personalization_discard_txt.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (parentActivity == "tutorial") {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                finish()
            }
        }
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
                } else if (usePIcons) {
                    "p"
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
                } else if (usePIcons) {
                    "p"
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
        updateColors(backgroundColor, usePalette)
        if (force || this.accentColor != accentColor) {
            this.accentColor = accentColor
            if (!useStockMultiIcons && !usePIcons) {
                for (icon: ImageView? in settingsIcons) {
                    icon?.setColorFilter(accentColor)
                }
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

            aosp_icons.buttonTintList = buttonColor
            stock_icons.buttonTintList = buttonColor
            stock_icons_multi.buttonTintList = buttonColor
            p_icons.buttonTintList = buttonColor

            white_notifications.buttonTintList = buttonColor
            dark_notifications.buttonTintList = buttonColor
            shadow_enabled.buttonTintList = buttonColor
            shadow_disabled.buttonTintList = buttonColor

            right_clock.buttonTintList = buttonColor
            left_clock.buttonTintList = buttonColor
            centered_clock.buttonTintList = buttonColor

            default_style.buttonTintList = buttonColor
            p_style.buttonTintList = buttonColor

            personalize_fab.background.setTint(accentColor)

            baseThemeInfo.setTextColor(accentColor)
        }
        if (force || this.backgroundColor != backgroundColor) {
            materialPalette = MaterialPalette.createPalette(backgroundColor, usePalette)
            this.backgroundColor = backgroundColor
            if (updateHex && hex_input_bg.text.toString() != Integer.toHexString(backgroundColor).substring(2))
                hex_input_bg.setText(Integer.toHexString(backgroundColor).substring(2), TextView.BufferType.EDITABLE)
            setBgIndicator()

            val sheetBg = getDrawable(R.drawable.personalization_sheet_bg) as LayerDrawable
            sheetBg.findDrawableByLayerId(R.id.sheet_bg).setTint(backgroundColor)
        }

        if (notifShadow) {
            preview_sysui_sender.text = getString(R.string.dark_notifications)
            preview_sysui_msg.text = getString(R.string.dark_notifications_preview)
            preview_sysui_msg.setText(R.string.notification_fix_summary)
            preview_sysui_app_title.setShadowLayer(2.0f, -1.0f, -1.0f, Color.WHITE)
            preview_sysui_sender.setTextColor(Color.BLACK)
            preview_sysui_sender.setShadowLayer(2.0f, -1.0f, -1.0f, Color.WHITE)
        } else {
            preview_sysui_sender.setText(R.string.dark_notifications)
            preview_sysui_msg.text = getString(R.string.dark_notifications_preview_normal)
            preview_sysui_app_title.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
            preview_sysui_sender.setTextColor(Color.WHITE)
            preview_sysui_sender.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
        }

        if (usePStyle) {
            notif_bg_layout.setImageResource(R.drawable.notif_bg_rounded)
        } else {
            notif_bg_layout.setImageResource(R.drawable.notif_bg)
        }

        if (darkNotif) {
            if (usePStyle) {
                notif_bg_layout.drawable.setTint(Color.parseColor("#0Dffffff"))
            } else {
                notif_bg_layout.drawable.setTint(Color.TRANSPARENT)
            }
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
