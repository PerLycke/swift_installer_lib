package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*

class OOSQRomHandler(context: Context) : QRomHandler(context) {

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {
        super.postInstall(uninstall, apps, oppositeApps, intent)
        if (apps.contains("android")) {
            runCommand("settings put system oem_black_mode_accent_color \'#${Integer.toHexString(
                    context.swift.selection.accentColor)}\'", true)

        }
    }

    override fun getChangelogTag(): String {
        return "oos-q"
    }

    override fun getDisabledOverlays(): SynchronizedArrayList<String> {
        return synchronizedArrayListOf(
                "com.google.android.marvin.talkback",
                "com.touchtype.swiftkey"
        )
    }

    override fun getRequiredApps(): Array<String> {
        return arrayOf(
                "android",
                "com.android.systemui"
        )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("42a5f5")
    }


    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {

            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["notif_background"] = "dark"
                selection["qs_alpha"] = "0"
                selection["sbar_icons_color"] = "grey"
                return selection
            }

            override fun populateCustomizeOptions(categories: CategoryMap) {
                populatePieCustomizeOptions(categories)
                super.populateCustomizeOptions(categories)
            }
            override fun createPreviewHandler(context: Context): PreviewHandler {
                return PiePreviewHandler(context)
            }
        }
    }
    class PiePreviewHandler(context: Context) : PreviewHandler(context) {
        override fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
            super.updateView(palette, selection)
            val darkNotif = (selection["notif_background"]) == "dark"
            systemUiPreview?.let {
                it.notif_bg_layout.setImageResource(R.drawable.notif_bg_rounded)
                if (darkNotif) {
                    it.notif_bg_layout.drawable.setTint(
                            ColorUtils.handleColor(palette.backgroundColor, 8))
                } else {
                    it.notif_bg_layout.drawable.setTint(
                            context.getColor(R.color.notification_bg_light))

                }
            }
        }
        override fun updateIcons(selection: CustomizeSelection) {
            super.updateIcons(selection)
            settingsIcons.forEach { icon ->
                icon.clearColorFilter()
                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_p"
                val id = context.resources.getIdentifier(
                        "${context.packageName}:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
            }
            systemUiIcons.forEach { icon ->
                val idName =
                        "ic_${context.resources.getResourceEntryName(icon.id)}_aosp"
                val id = context.resources.getIdentifier(
                        "${context.packageName}:drawable/$idName", null, null)
                if (id > 0) {
                    val layerDrawable = context.getDrawable(id) as LayerDrawable
                    icon.setImageDrawable(layerDrawable)
                    layerDrawable.findDrawableByLayerId(R.id.icon_bg)
                            .setTint(selection.accentColor)
                    layerDrawable.findDrawableByLayerId(
                            R.id.icon_tint).setTint(selection.backgroundColor)
                }
            }
        }


    }
    override fun populatePieCustomizeOptions(categories: CategoryMap) {

        val qsOptions = OptionsMap()
        val trans =
                SliderOption(context.getString(R.string.qs_transparency), "qs_alpha")
        trans.current = 0
        qsOptions.add(trans)
        categories.add(CustomizeCategory(context.getString(R.string.quick_settings_style),
                "qs_alpha", "0", qsOptions,
                synchronizedArrayListOf("android")))

        val sbarIconColorOptions = OptionsMap()
        sbarIconColorOptions.add(Option(context.getString(R.string.sbar_icons_color_default), "default", infoDialogTitle = context.getString(R.string.sbar_icons_color_default_dialog_title), infoDialogText = context.getString(R.string.sbar_icons_color_default_dialog_text)))
        sbarIconColorOptions.add(Option(context.getString(R.string.sbar_icons_color_white), "white", infoDialogTitle = context.getString(R.string.sbar_icons_color_white_dialog_title), infoDialogText = context.getString(R.string.sbar_icons_color_white_dialog_text)))
        sbarIconColorOptions.add(Option(context.getString(R.string.sbar_icons_color_grey), "grey", infoDialogTitle = context.getString(R.string.sbar_icons_color_grey_dialog_title), infoDialogText = context.getString(R.string.sbar_icons_color_grey_dialog_text)))
        sbarIconColorOptions.add(Option(context.getString(R.string.sbar_icons_color_accent), "accent", infoDialogTitle = context.getString(R.string.sbar_icons_color_accent_dialog_title), infoDialogText = context.getString(R.string.sbar_icons_color_accent_dialog_text)))

        categories.add(CustomizeCategory(context.getString(R.string.sbar_icons_color_category), "sbar_icons_color", "stock", sbarIconColorOptions, synchronizedArrayListOf("com.android.systemui")))
    }
}