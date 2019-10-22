package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.graphics.drawable.LayerDrawable
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*

class MiuiQRomHandler(context: Context) : QRomHandler(context) {

    override fun getChangelogTag(): String {
        return "miui-q"
    }

    override fun getDisabledOverlays(): SynchronizedArrayList<String> {
        return synchronizedArrayListOf(
                "com.google.android.marvin.talkback",
                "com.android.settings",
                "com.android.settings.intelligence",
                "com.android.providers.media",
                "com.android.bluetooth",
                "com.android.contacts",
                "com.android.emergency",
                "com.android.deskclock",
                "com.android.phone",
                "com.android.server.telecom",
                "com.miui.calculator",
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
        return ColorUtils.convertToColorInt("0099ff")
    }


    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {

            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["qs_alpha"] = "0"
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
    }
}