/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.CircleDrawable
import com.brit.swiftinstaller.library.ui.customize.CustomizeHandler
import com.brit.swiftinstaller.library.ui.customize.CustomizeSelection
import com.brit.swiftinstaller.library.ui.customize.Option
import com.brit.swiftinstaller.library.ui.customize.PreviewHandler
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.ColorUtils.checkAccentColor
import com.brit.swiftinstaller.library.utils.ColorUtils.checkBackgroundColor
import com.brit.swiftinstaller.library.utils.ColorUtils.convertToColorInt
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_customize.*
import kotlinx.android.synthetic.main.customize_accent.*
import kotlinx.android.synthetic.main.customize_background.*
import kotlinx.android.synthetic.main.customize_option_item.view.*
import kotlinx.android.synthetic.main.customize_option_layout.view.*
import kotlinx.android.synthetic.main.fab_sheet_personalize.view.*
import kotlinx.android.synthetic.main.palette_view.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class CustomizeActivity : ThemeActivity() {

    private lateinit var customizeHandler: CustomizeHandler
    private lateinit var previewHandler: PreviewHandler

    private lateinit var selection: CustomizeSelection

    private val backgroundIndicatorHandler = BackgroundIndicatorHandler()

    private var finish = false
    private var usePalette = false
    private var updateOnResume = false

    private lateinit var materialPalette: MaterialPalette
    private val handler = Handler()
    private var parentActivity: String? = "parent"
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var recompile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize)

        customizeHandler = swift.romHandler.getCustomizeHandler()
        previewHandler = customizeHandler.createPreviewHandler(this)
        selection = customizeHandler.getSelection()
        parentActivity = intent.getStringExtra("parentActivity")
        usePalette = useBackgroundPalette(this)
        materialPalette = MaterialPalette.createPalette(selection.backgroundColor, usePalette)

        personalize_fab.background.setTint(selection.accentColor)
        category_update_progress.indeterminateDrawable.setColorFilter(selection.accentColor,
                PorterDuff.Mode.SRC_ATOP)
        customizations_scrollview.isVerticalScrollBarEnabled = false

        setupPreview()
        setupAccentSheet()
        setupHexInputs()
        val handler = MessageQueue.IdleHandler {
            setupThemeOptions()
            false
        }
        Looper.myQueue().addIdleHandler(handler)
    }

    private fun showFab() {
        handler.postDelayed({
            if (personalize_fab.visibility == View.GONE) {
                personalize_fab.setVisible(true)
                personalize_fab.startAnimation(AnimationUtils.loadAnimation(this@CustomizeActivity,
                        android.R.anim.fade_in))
            }
            if (accent_hex_input.hasFocus() || hex_input_bg.hasFocus()) {
                personalize_fab.setImageDrawable(
                        ContextCompat.getDrawable(this@CustomizeActivity, R.drawable.ic_done))
            } else {
                personalize_fab.setImageDrawable(ContextCompat.getDrawable(this@CustomizeActivity,
                        R.drawable.ic_fab_install))
            }
        }, 300)
    }

    private fun hideFab() {
        if (personalize_fab.visibility == View.VISIBLE) {
            personalize_fab.setVisible(false)
        }
    }

    private fun setupPreview() {
        preview_pager.pageMargin = 64
        val adapter = PreviewPagerAdapter()
        handler.post {
            preview_pager.adapter = adapter
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
        accent_hex_input.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> accent_hex_input.clearFocus()
                }
            }
            false
        }
        hex_input_bg.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> hex_input_bg.clearFocus()
                }
            }
            false
        }

        accent_hex_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 3 || s?.length == 6) {
                    if (checkAccentColor(convertToColorInt(s.toString()))) {
                        selection.accentColor = convertToColorInt(s.toString())
                        updateColor(false)
                    } else {
                        toast(R.string.invalid_accent)
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
                        selection.backgroundColor = convertToColorInt(s.toString())
                        updateColor(false)
                    } else {
                        toast(R.string.invalid_background)
                    }
                }
            }
        })
    }

    inner class RadioGroup {
        private val buttons = SynchronizedArrayList<RadioButton>()

        fun addRadioButton(button: RadioButton) {
            buttons.add(button)
        }

        fun setCurrentButton(button: RadioButton) {
            buttons.forEach { but ->
                but.isChecked = but == button
            }
        }
    }

    private fun setupOption(optionsContainer: ViewGroup, option: Option, categoryKey: String,
                            group: RadioGroup) {
        val optionView =
                LayoutInflater.from(this).inflate(R.layout.customize_option_item, optionsContainer,
                        false) as ViewGroup
        if (option.isSliderOption) {
            optionView.slider_layout.setVisible(true)
            optionView.slider_title.text = option.name
            optionView.percent.text =
                    getString(R.string.alpha_value, selection.getInt(option.value))
            optionView.slider.progress = selection.getInt(option.value)
            optionView.slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    optionView.percent.text = getString(R.string.alpha_value, p1)
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    selection[option.value] = p0!!.progress.toString()
                    updateColor(false)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
            })
            optionView.option_button.setVisible(false)
        } else {
            optionView.option_button.isChecked = option.value == selection[categoryKey]
            optionView.option_button.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    group.setCurrentButton(compoundButton as RadioButton)
                    selection[categoryKey] = option.value
                    updateColor(false)
                }
                if (option.subOptions.isNotEmpty()) {
                    optionView.sub_options.setVisible(b)
                }
            }
            group.addRadioButton(optionView.option_button)
            optionView.option_button.text = option.name
            optionView.option_info.setOnClickListener {
                alert {
                    title = option.infoDialogTitle
                    message = option.infoDialogText
                    positiveButton(R.string.ok) { dialog ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
            optionView.option_info.setVisible(option.infoDialogText.isNotEmpty())
            optionView.rounded_divider.setVisible(option.infoDialogText.isNotEmpty())
            if (option.subOptions.isNotEmpty()) {
                if (option.infoText.isNotEmpty()) {
                    optionView.option_info_text.text = option.infoText
                }
                val subGroup = RadioGroup()
                option.subOptions.reversed().forEach { subOption ->
                    setupOption(optionView.sub_options_container, subOption, option.subOptionKey,
                            subGroup)
                }
            }
        }
        optionsContainer.addView(optionView)
    }

    private fun setupThemeOptions() {
        doAsync {
            val views = arrayListOf<View>()
            customizeHandler.getCustomizeOptions().reversed().forEach { category ->
                val categoryView = LayoutInflater.from(this@CustomizeActivity)
                        .inflate(R.layout.customize_option_layout, customize_options, false)
                categoryView.customize_category_title.text = category.name
                val optionGroup = RadioGroup()
                category.options.forEachOption { option ->
                    setupOption(categoryView.options, option, category.key, optionGroup)
                }
                views.add(categoryView)
            }

            val baseThemeListener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (compoundButton.id == R.id.material_theme) {
                    material_theme.isChecked = b
                    flat_theme.isChecked = !b
                } else {
                    material_theme.isChecked = !b
                    flat_theme.isChecked = b
                }
                usePalette = material_theme.isChecked
                updateColor(false)
            }

            material_theme.setOnCheckedChangeListener(baseThemeListener)
            flat_theme.setOnCheckedChangeListener(baseThemeListener)

            uiThread {
                for (v in views) {
                    customize_options.addView(v)
                }

                material_theme.isChecked = usePalette
                flat_theme.isChecked = !usePalette

                updateColor(true)
                updateOnResume = true
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (parentActivity == "tutorial") {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (updateOnResume) {
            updateColor(true)
        }
        if (finish) finish()
    }

    private fun checkAndAddApp(apps: SynchronizedArrayList<String>, app: String) {
        if (!apps.contains(app) && swift.romHandler.isOverlayInstalled("android")) {
            apps.add(app)
            recompile = true
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun baseThemeInfoClick(view: View) {
        alert {
            this.title = getString(R.string.base_theme_dialog_title)
            this.message = getString(R.string.base_theme_dialog_info)
            positiveButton(R.string.ok) { d ->
                d.dismiss()
            }
            show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun personalizeFabClick(view: View) {

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
        sheetView.setBackgroundColor(selection.backgroundColor)

        if (parentActivity == "tutorial") {
            sheetView.personalization_confirm_txt.text = getString(R.string.continue_theming)
            sheetView.personalization_discard_txt.text = getString(R.string.exit_to_main)
        }
        bottomSheetDialog.show()

        sheetView.personalization_confirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            val apps = SynchronizedArrayList<String>()

            val oldPalette = useBackgroundPalette(this)
            val oldSelection = customizeHandler.getSelection()

            fun hotSwapPrefOn() = PreferenceManager.getDefaultSharedPreferences(
                    this).edit().putBoolean("hotswap", true).apply()

            fun hotSwapPrefOff() = PreferenceManager.getDefaultSharedPreferences(
                    this).edit().putBoolean("hotswap", false).apply()

            if (selection.accentColor != oldSelection.accentColor) {
                if (swift.romHandler.useHotSwap()) {
                    hotSwapPrefOn()
                }
                checkAndAddApp(apps, "android")
                if (swift.romHandler.isOverlayInstalled("com.touchtype.swiftkey")) {
                    checkAndAddApp(apps, "com.touchtype.swiftkey")
                }
            }

            oldSelection.keys.forEach { key ->
                if (oldSelection[key] != selection[key]) {
                    val cat = customizeHandler.getCustomizeOptions()[key]
                    hotSwapPrefOff()
                    cat?.requiredApps?.forEach { app ->
                        checkAndAddApp(apps, app)
                    }
                }
            }

            if (selection.backgroundColor != oldSelection.backgroundColor) {
                hotSwapPrefOff()
                checkAndAddApp(apps, "android")
                if (swift.romHandler.isOverlayInstalled("com.touchtype.swiftkey")) {
                    checkAndAddApp(apps, "com.touchtype.swiftkey")
                }
            }

            swift.selection = selection

            if (usePalette != oldPalette) {
                setUseBackgroundPalette(this, usePalette)
                hotSwapPrefOff()
                checkAndAddApp(apps, "android")
            }

            if (recompile && apps.isNotEmpty()) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (intent.action == InstallSummaryActivity.ACTION_INSTALL_CANCELLED) {
                            selection.keys.forEach { key ->
                                if (oldSelection[key] != selection[key]) {
                                    selection[key] = oldSelection[key]
                                }
                            }
                            customizeHandler.setSelection(selection)
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
                if (parentActivity == "tutorial") {
                    val intent = Intent(this, OverlaysActivity::class.java)
                    val bundle = Bundle()
                    intent.putExtras(bundle)
                    startActivity(intent)
                } else {
                    finish()
                }
            }
        }

        sheetView.personalization_cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (parentActivity == "tutorial") {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                finish()
            }
        }
    }

    private fun setupAccentSheet() {
        accent_palette.adapter = PaletteAdapter(customizeHandler.getAccentColors(), true)
        background_palette.adapter = PaletteAdapter(customizeHandler.getBackgroundColors(), false)
    }

    fun updateColor(updateHex: Boolean) {
        updateColors(selection.backgroundColor, usePalette)
        IdLists.radioButtons.forEach { id ->
            val b = findViewById<RadioButton>(id)
            b.buttonTintList = ColorUtils.radioButtonColor(this, R.color.radio_button_disabled,
                    selection.accentColor)
            b.jumpDrawablesToCurrentState()
        }

        backgroundIndicatorHandler.setIndicatorColor(selection.accentColor)
        backgroundIndicatorHandler.setActiveIndicator(selection.backgroundColor)

        val buttonTint = ColorUtils.radioButtonColor(this, R.color.radio_button_disabled,
                selection.accentColor)
        customize_options.childCount.downTo(0).forEach { position ->
            val view = customize_options.getChildAt(position)
            if (view != null) {
                if (view.options.childCount > 0) {
                    view.options.childCount.downTo(0).forEach { pos ->
                        val v = view.options.getChildAt(pos)
                        if (v != null) {
                            v.option_button.buttonTintList = buttonTint
                            v.option_info.setTextColor(selection.accentColor)
                            if (v.sub_options_container.childCount > 0) {
                                v.sub_options_container.childCount.downTo(0).forEach { posi ->
                                    val vi = v.sub_options_container.getChildAt(posi)
                                    if (vi != null) {
                                        vi.option_button.buttonTintList = buttonTint
                                        vi.option_info.setTextColor(selection.accentColor)
                                    }
                                }
                            }
                            v.slider.thumb.setColorFilter(selection.accentColor,
                                    PorterDuff.Mode.SRC_ATOP)
                            v.slider.progressDrawable.setColorFilter(selection.accentColor,
                                    PorterDuff.Mode.SRC_ATOP)
                        }
                    }
                }
            }
        }

        if (updateHex && accent_hex_input.text.toString() != Integer.toHexString(
                        selection.accentColor).substring(2))
            accent_hex_input.setText(Integer.toHexString(selection.accentColor).substring(2),
                    TextView.BufferType.EDITABLE)

        accent_hex_input.background.setTint(selection.accentColor)
        hex_input_bg.background.setTint(selection.accentColor)
        personalize_fab.background.setTint(selection.accentColor)
        baseThemeInfo.setTextColor(selection.accentColor)

        materialPalette = MaterialPalette.createPalette(selection.backgroundColor, usePalette)

        if (updateHex && hex_input_bg.text.toString() != Integer.toHexString(
                        materialPalette.backgroundColor).substring(2))
            hex_input_bg.setText(Integer.toHexString(materialPalette.backgroundColor).substring(2),
                    TextView.BufferType.EDITABLE)

        previewHandler.updateView(materialPalette, selection)

        if (main_layout.visibility == View.INVISIBLE) {
            category_update_progress.visibility = View.INVISIBLE
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 500
            main_layout.visibility = View.VISIBLE
            main_layout.startAnimation(anim)
            customizations_scrollview.isVerticalScrollBarEnabled = true
        }
    }

    class BackgroundIndicatorHandler {
        private val indicators = arrayListOf<ImageView>()
        fun addIndicator(indicator: ImageView) {
            indicators.add(indicator)
        }

        fun setIndicatorColor(color: Int) {
            indicators.forEach {
                it.drawable.setTint(color)
            }
        }

        fun setActiveIndicator(color: Int) {
            indicators.forEach {
                it.setVisible(it.tag == color)
            }
        }
    }

    inner class PaletteAdapter constructor(
            private val colors: SynchronizedArrayList<CustomizeHandler.PaletteItem>,
            private val isAccent: Boolean) : BaseAdapter() {

        override fun getCount(): Int {
            return colors.size
        }

        override fun getItem(position: Int): Any {
            return colors[position]
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
            if (isAccent) {
                mainView!!.accent_layout.setVisible(isAccent)
                mainView.accent_layout.icon.background =
                        CircleDrawable(colors[position].accentColor)
                mainView.tag = colors[position]
                mainView.setOnClickListener {
                    selection.accentColor = colors[position].accentColor
                    updateColor(true)
                }
            } else {
                mainView!!.background_layout.setVisible(!isAccent)
                mainView.background_layout.indicator.tag = colors[position].backgroundColor
                mainView.background_layout.indicator.drawable.setTint(selection.accentColor)
                mainView.background_layout.indicator.setVisible(
                        selection.backgroundColor == colors[position].backgroundColor)
                backgroundIndicatorHandler.addIndicator(mainView.background_layout.indicator)
                mainView.background_layout.background_icon.tag = colors[position].backgroundColor
                mainView.background_layout.background_icon.setImageDrawable(
                        CircleDrawable(colors[position].backgroundColor))
                mainView.background_layout.background_title.text = colors[position].backgroundName
                mainView.background_layout.background_icon.setOnClickListener {
                    backgroundIndicatorHandler.setActiveIndicator(colors[position].backgroundColor)
                    selection.backgroundColor = colors[position].backgroundColor
                    updateColor(true)
                }
                mainView.isEnabled = false
            }
            return mainView
        }
    }

    inner class PreviewPagerAdapter : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val view = previewHandler.getPage(collection, position)
            collection.addView(view)
            previewHandler.updateView(materialPalette, selection)
            return view
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return previewHandler.getPageCount()
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

    }
}
