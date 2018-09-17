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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
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

class CustomizeActivity : ThemeActivity() {

    private lateinit var customizeHandler: CustomizeHandler
    private lateinit var previewHandler: PreviewHandler

    private lateinit var selection: CustomizeSelection

    private var finish = false
    private var usePalette = false

    private lateinit var materialPalette: MaterialPalette
    private val handler = Handler()
    private var parentActivity: String? = "parent"
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var recompile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customizeHandler = swift.romInfo.getCustomizeHandler()
        previewHandler = customizeHandler.createPreviewHandler(this)
        selection = customizeHandler.getSelection()

        parentActivity = intent.getStringExtra("parentActivity")

        setContentView(R.layout.activity_customize)
        handler.post {
            setupAccentSheet()
            usePalette = useBackgroundPalette(this)
            setupHexInputs()
            setupThemeOptions()

            val viewpager: ViewPager = findViewById(R.id.preview_pager)
            viewpager.pageMargin = 64
            val adapter = PreviewPagerAdapter()
            viewpager.adapter = adapter

            personalize_fab.setOnClickListener {
                personalizeFabClick()
            }

            val infoListener: (String, String) -> View.OnClickListener = { title, message ->
                View.OnClickListener {
                    val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                    val dialogBg = getDrawable(R.drawable.dialog_bg) as LayerDrawable
                    dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(selection.backgroundColor)
                    builder.setTitle(title)
                    builder.setMessage(message)
                    builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(selection.backgroundColor)
                }
            }

            baseThemeInfo.setOnClickListener(infoListener(getString(R.string.base_theme_dialog_title), getString(R.string.base_theme_dialog_info)))
            baseThemeInfo.setOnClickListener(infoListener(getString(R.string.base_theme_dialog_title), getString(R.string.base_theme_dialog_info)))

            updateColor(true)
        }
    }

    @SuppressLint("RestrictedApi")
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

    @SuppressLint("RestrictedApi")
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
                        selection.backgroundColor = convertToColorInt(s.toString())
                        updateColor(false)
                    } else {
                        Toast.makeText(this@CustomizeActivity,
                                R.string.invalid_background, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    inner class RadioGroup {
        private val buttons = ArrayList<RadioButton>()

        fun addRadioButton(button: RadioButton) {
            buttons.add(button)
        }

        fun setCurrentButton(button: RadioButton) {
            for (but in buttons) {
                but.isChecked = but == button
            }
        }
    }

    private fun setupOption(optionsContainer: ViewGroup, option: Option, categoryKey: String, group: RadioGroup) {
        val optionView = LayoutInflater.from(this).inflate(R.layout.customize_option_item, optionsContainer, false) as ViewGroup
        if (option.isSliderOption) {
            optionView.slider_layout.setVisible(true)
            optionView.slider_title.text = option.name
            optionView.percent.text = getString(R.string.alpha_value, selection[option.value].toInt())
            optionView.slider.progress = selection[option.value].toInt()
            optionView.slider.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
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
                val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                val dialogBg = getDrawable(R.drawable.dialog_bg) as LayerDrawable
                dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(selection.backgroundColor)
                builder.setTitle(option.infoDialogTitle)
                builder.setMessage(option.infoDialogText)
                builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                val dialog = builder.create()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(selection.accentColor)
                dialog.show()
            }
            optionView.option_info.setVisible(option.infoDialogText.isNotEmpty())
            optionView.rounded_divider.setVisible(option.infoDialogText.isNotEmpty())
            if (option.subOptions.isNotEmpty()) {
                if (option.infoText.isNotEmpty()) {
                    optionView.option_info_text.text = option.infoText
                }
                val subGroup = RadioGroup()
                for (subOption in option.subOptions.reversed()) {
                    setupOption(optionView.sub_options_container, subOption, option.subOptionKey,
                            subGroup)
                }
            }
        }
        optionsContainer.addView(optionView)
    }

    private fun setupThemeOptions() {
        material_theme.isChecked = usePalette
        flat_theme.isChecked = !usePalette

        for (category in customizeHandler.getCustomizeOptions().reversed()) {
            val categoryView = LayoutInflater.from(this).inflate(R.layout.customize_option_layout, customize_options, false)
            categoryView.customize_category_title.text = category.name
            val optionGroup = RadioGroup()
            for (option in category.options) {
                setupOption(categoryView.options, option, category.key, optionGroup)
            }
            customize_options.addView(categoryView)
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
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (parentActivity == "tutorial") {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateColors(selection.backgroundColor, usePalette)
        if (finish) finish()
    }

    private fun checkAndAddApp(apps: ArrayList<String>, app: String) {
        if (!apps.contains(app) && swift.romInfo.isOverlayInstalled("android")) {
            apps.add(app)
            recompile = true
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
        sheetView.setBackgroundColor(selection.backgroundColor)

        if (parentActivity == "tutorial") {
            sheetView.personalization_confirm_txt.text = getString(R.string.continue_theming)
            sheetView.personalization_discard_txt.text = getString(R.string.exit_to_main)
        }
        bottomSheetDialog.show()

        sheetView.personalization_confirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            val apps = ArrayList<String>()

            val oldPalette = useBackgroundPalette(this)
            val oldSelection = customizeHandler.getSelection()

            fun hotSwapPrefOn() = PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("hotswap", true).apply()
            fun hotSwapPrefOff() = PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("hotswap", false).apply()

            for (key in oldSelection.keys) {
                if (oldSelection[key] != selection[key]) {
                    val cat = customizeHandler.getCustomizeOptions()[key]
                    if (cat != null) {
                        for (app in cat.requiredApps) {
                            if (app == "android") {
                                if (swift.romInfo.useHotSwap()) {
                                    hotSwapPrefOn()
                                } else {
                                    hotSwapPrefOff()
                                }
                            }
                            checkAndAddApp(apps, app)
                        }
                    }
                }
            }
            customizeHandler.setSelection(selection)

            if (usePalette != oldPalette) {
                setUseBackgroundPalette(this, usePalette)
                hotSwapPrefOff()
                checkAndAddApp(apps, "android")
            }

            if (recompile && apps.isNotEmpty()) {

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (intent.action == InstallSummaryActivity.ACTION_INSTALL_CANCELLED) {
                            for (key in selection.keys) {
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
                            if (!Utils.isSamsungOreo()) {
                                getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "default").apply()
                                startActivity(intent)
                            } else {
                                val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                                        .setTitle(R.string.reboot_delay_title)
                                        .setMessage(R.string.reboot_delay_msg)
                                        .setPositiveButton(R.string.proceed) { dialogInterface, _ ->
                                            getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "default").apply()
                                            dialogInterface.dismiss()
                                            startActivity(intent)
                                        }
                                        .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                                            dialogInterface.dismiss()
                                        }

                                themeDialog()
                                val dialog = builder.create()
                                dialog.show()
                            }
                        }
                        else -> {
                            val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                                    .setTitle(R.string.installing_and_uninstalling_title)
                                    .setMessage(R.string.installing_and_uninstalling_msg)
                                    .setPositiveButton(R.string.proceed) { dialogInterface, _ ->
                                        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("thisLaunched", true).apply()
                                        dialogInterface.dismiss()
                                        startActivity(intent)
                                    }

                            themeDialog()
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                }
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
        for (id in IdLists.radioButtons) {
            val b = findViewById<RadioButton>(id)
            b.buttonTintList = ColorUtils.radioButtonColor(this, R.color.radio_button_disabled, selection.accentColor)
        }

        val buttonTint = ColorUtils.radioButtonColor(this, R.color.radio_button_disabled, selection.accentColor)
        for (position in customize_options.childCount.downTo(0)) {
            val view = customize_options.getChildAt(position)
            if (view != null) {
                if (view.options.childCount > 0) {
                    for (pos in view.options.childCount.downTo(0)) {
                        val v = view.options.getChildAt(pos)
                        if (v != null) {
                            v.option_button.buttonTintList = buttonTint
                            v.option_info.setTextColor(selection.accentColor)
                            if (v.sub_options_container.childCount > 0) {
                                for (posi in v.sub_options_container.childCount.downTo(0)) {
                                    val vi = v.sub_options_container.getChildAt(posi)
                                    if (vi != null) {
                                        vi.option_button.buttonTintList = buttonTint
                                        vi.option_info.setTextColor(selection.accentColor)
                                    }
                                }
                            }
                            v.slider.thumb.setColorFilter(selection.accentColor, PorterDuff.Mode.SRC_ATOP)
                            v.slider.progressDrawable.setColorFilter(selection.accentColor,
                                    PorterDuff.Mode.SRC_ATOP)
                        }
                    }
                }
            }
        }

        if (updateHex && accent_hex_input.text.toString() != Integer.toHexString(selection.accentColor).substring(2))
            accent_hex_input.setText(Integer.toHexString(selection.accentColor).substring(2), TextView.BufferType.EDITABLE)

        accent_hex_input.background.setTint(selection.accentColor)
        hex_input_bg.background.setTint(selection.accentColor)
        personalize_fab.background.setTint(selection.accentColor)
        baseThemeInfo.setTextColor(selection.accentColor)

        materialPalette = MaterialPalette.createPalette(selection.backgroundColor, usePalette)

        if (updateHex && hex_input_bg.text.toString() != Integer.toHexString(materialPalette.backgroundColor).substring(2))
            hex_input_bg.setText(Integer.toHexString(materialPalette.backgroundColor).substring(2), TextView.BufferType.EDITABLE)

        previewHandler.updateBackgroundColor(materialPalette)
        previewHandler.updateView(materialPalette, selection)
    }

    class BackgroundIndicatorHandler {
        private val indicators = arrayListOf<ImageView>()
        fun addIndicator(indicator: ImageView) {
            indicators.add(indicator)
        }
        fun setActiveIndicator(indicator: ImageView) {
            for (ind in indicators) {
                ind.setVisible(ind.tag == indicator.tag)
            }
        }
    }

    inner class PaletteAdapter constructor(private val colors: ArrayList<CustomizeHandler.PaletteItem>, private val isAccent: Boolean) : BaseAdapter() {

        private val indicatorHandler = BackgroundIndicatorHandler()

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
                mainView.accent_layout.icon.background = CircleDrawable(colors[position].accentColor)
                mainView.tag = colors[position]
                mainView.setOnClickListener {
                    selection.accentColor = colors[position].accentColor
                    updateColor(true)
                }
            } else {
                mainView!!.background_layout.setVisible(!isAccent)
                indicatorHandler.addIndicator(mainView.background_layout.indicator)
                mainView.background_layout.background_icon.tag = colors[position].backgroundColor
                mainView.background_layout.background_icon.setImageDrawable(CircleDrawable(colors[position].backgroundColor))
                mainView.background_layout.background_title.text = colors[position].backgroundName
                mainView.background_layout.background_icon.setOnClickListener {
                    indicatorHandler.setActiveIndicator(mainView.background_layout.indicator)
                    selection.backgroundColor = colors[position].backgroundColor
                    updateColor(true)
                }
            }
            return mainView
        }
    }

    inner class PreviewPagerAdapter : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val view = previewHandler.getPage(collection, position)
            collection.addView(view)
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
