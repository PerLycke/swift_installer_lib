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

package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.topjohnwu.superuser.io.SuFile
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*

open class PRomHandler(context: Context) : RomHandler(context) {

    private val systemApp = "/system/app"

    private val appPath: String

    override val magiskEnabled: Boolean by lazy {
        if (!moduleDisabled && !SuFile(magiskPath).exists()) {
            MagiskUtils.createModule(context)
        }
        super.magiskEnabled
    }

    init {
        appPath = if (!moduleDisabled && SuFile(magiskPath).exists()) {
            val f = SuFile(magiskPath, systemApp)
            if (!f.exists()) {
                f.mkdirs()
            }
            f.absolutePath
        } else {
            systemApp
        }
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        val overlayPackage = getOverlayPackageName(targetPackage)
        if (ShellUtils.isRootAvailable) {
            if (!magiskEnabled) remountRW("/system")
            ShellUtils.mkdir("$appPath/$overlayPackage")
            ShellUtils.copyFile(overlayPath, "$appPath/$overlayPackage/$overlayPackage.apk")
            ShellUtils.setPermissions(755, "$appPath/$overlayPackage/$overlayPackage.apk")
            if (!magiskEnabled) remountRO("/system")
        }
    }

    override fun getRequiredApps(): Array<String> {
        return arrayOf(
                "android",
                "com.android.systemui",
                "com.amazon.clouddrive.photos",
                "com.android.settings",
                "com.anydo",
                "com.apple.android.music",
                "com.ebay.mobile",
                "com.embermitre.pixolor.app",
                "com.google.android.apps.genie.geniewidget",
                "com.google.android.apps.inbox",
                "com.google.android.gm",
                "com.google.android.talk",
                "com.mxtech.videoplayer.ad",
                "com.mxtech.videoplayer.pro",
                "com.pandora.android",
                "com.simplecity.amp.pro",
                "com.Slack",
                "com.twitter.android",
                "com.google.android.gms",
                "com.google.android.apps.nexuslauncher",
                "com.lastpass.lpandroid",
                "com.weather.Weather",
                "com.google.android.settings.intelligence",
                "com.google.android.inputmethod.latin",
                "com.google.intelligence.sense"
        )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("1a73e8")
    }

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {

        if (!uninstall && oppositeApps.isNotEmpty()) {
            oppositeApps.forEach { app ->
                uninstallOverlay(context, app)
            }
        }

        if (intent != null) {
            context.applicationContext.startActivity(intent)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        val overlayPackage = getOverlayPackageName(packageName)
        if (ShellUtils.isRootAvailable) {
            if (!magiskEnabled) remountRW("/system")
            deleteFileRoot("$appPath/$overlayPackage/")
            if (!magiskEnabled) remountRO("/system")
        }
    }

    override fun isOverlayInstalled(targetPackage: String): Boolean {
        val overlayPackage = getOverlayPackageName(targetPackage)
        return SuFile("$appPath/$overlayPackage/$overlayPackage.apk").exists()
    }

    private fun getOverlayPath(packageName: String): String {
        val overlayPackage = getOverlayPackageName(packageName)
        return "$appPath/$overlayPackage/$overlayPackage.apk"
    }

    override fun getOverlayInfo(pm: PackageManager, packageName: String): PackageInfo {
        return pm.getPackageArchiveInfo(getOverlayPath(packageName),
                PackageManager.GET_META_DATA)
    }

    override fun useHotSwap(): Boolean {
        return true
    }

    override fun getChangelogTag(): String {
        return "p"
    }

    override fun onBootCompleted(context: Context) {
        if (magiskEnabled && !moduleDisabled && !context.disableMagisk) {
            MagiskUtils.convertToMagisk(context)
        } else {
            MagiskUtils.convertFromMagisk(context)
        }
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {
            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["stock_pie_icons"] = "default_icons"
                selection["notif_background"] = "dark"
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
                                val drawable = context.getDrawable(id)?.mutate() as LayerDrawable
                                if (selection["stock_pie_icons"] == "stock_accented") {
                                    drawable.findDrawableByLayerId(R.id.icon_bg)
                                            .setTint(selection.accentColor)
                                    drawable.findDrawableByLayerId(R.id.icon_fg)
                                            .setTint(selection.backgroundColor)
                                }
                                icon.setImageDrawable(drawable)
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

    open fun populatePieCustomizeOptions(categories: CategoryMap) {
        val pieIconOptions = OptionsMap()
        pieIconOptions.add(Option(context.getString(R.string.stock_icons_multi), "default_icons"))
        pieIconOptions.add(Option(context.getString(R.string.stock_icons), "stock_accented"))
        categories.add(
                CustomizeCategory(context.getString(R.string.pie_category_setting_icon), "stock_pie_icons",
                        "default_icons", pieIconOptions,
                        synchronizedArrayListOf("com.android.settings",
                                "com.google.android.apps.wellbeing",
                                "com.google.android.gms",
                                "com.cyanogenmod.settings.device",
                                "com.du.settings.doze",
                                "com.moto.actions",
                                "com.slim.device")))
        val notifBackgroundOptions = OptionsMap()
        notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
        notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
        categories.add(CustomizeCategory(context.getString(R.string.notification_tweaks),
                "notif_background", "white", notifBackgroundOptions, synchronizedArrayListOf("android")))

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