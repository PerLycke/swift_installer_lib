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
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPath
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import kotlinx.android.synthetic.main.customize_preview_settings.view.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*
import java.io.File

class SamsungRomHandler(context: Context) : RomHandler(context) {

    override fun requiresRoot(): Boolean {
        return false
    }

    override fun getChangelogTag(): String {
        return "samsung"
    }

    override fun addTutorialSteps(tutorial: TutorialActivity) {
        super.addTutorialSteps(tutorial)
        tutorial.addFragment(
                Step.Builder().setTitle(tutorial.getString(R.string.tutorial_more_usage_title))
                        .setContent(tutorial.getString(R.string.tutorial_more_usage_info))
                        .setBackgroundColor(
                                ContextCompat.getColor(tutorial, R.color.background_main))
                        .setDrawable(R.drawable.ic_tutorial_clicks) // int top drawable
                        .build(), TUTORIAL_PAGE_PERMISSIONS + addToIndex())
    }

    override fun getDisabledOverlays(): SynchronizedArrayList<String> {
        return synchronizedArrayListOf(
                "com.android.emergency"
        )
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
                "com.google.android.packageinstaller",
                "com.mxtech.videoplayer.ad",
                "com.mxtech.videoplayer.pro",
                "com.pandora.android",
                "com.simplecity.amp.pro",
                "com.Slack",
                "com.samsung.android.incallui",
                "com.twitter.android",
                "com.samsung.android.contacts",
                "com.samsung.android.themestore",
                "com.samsung.android.lool",
                "com.samsung.android.samsungpassautofill",
                "com.google.android.gms",
                "com.sec.android.daemonapp",
                "de.axelspringer.yana.zeropage",
                "com.google.android.apps.nexuslauncher",
                "com.lastpass.lpandroid",
                "com.weather.Weather",
                "com.google.android.inputmethod.latin"
        )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("3dbce9")
    }

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {
        val extraIntent = intent != null

        if (ShellUtils.isRootAvailable) {
            if (!uninstall && oppositeApps.isNotEmpty()) {
                oppositeApps.forEach { app ->
                    uninstallOverlay(context, app)
                }
            }
            if (intent != null) {
                context.applicationContext.startActivity(intent)
            }
            return
        }

        if (apps.contains("android")) {
            val index = apps.indexOf("android")
            apps.removeAt(index)
            apps.add(0, "android")
        }
        if (apps.contains("com.google.android.packageinstaller")) {
            val index = apps.indexOf("com.google.android.packageinstaller")
            apps.removeAt(index)
            apps.add(0, "com.google.android.packageinstaller")
        }

        val intents = Array(if (!extraIntent) {
            apps.size
        } else {
            apps.size + 1
        }) { i ->
            val index = if (extraIntent) {
                i - 1
            } else {
                i
            }
            if (!extraIntent || i > 0) {
                val appInstall = Intent()
                if (uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(apps.elementAt(index)), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            "${context.packageName}.myprovider",
                            File(getOverlayPath(apps.elementAt(index))))
                }
                appInstall.addCategory(Intent.CATEGORY_DEFAULT)
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            } else {
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
        }
        if (!intents.isEmpty()) {
            context.startActivities(intents)
        }

        if (!oppositeApps.isEmpty()) {
            val oppositeIntents = Array(oppositeApps.size) {
                val appInstall = Intent()
                if (!uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(oppositeApps[it]), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".myprovider",
                            File(getOverlayPath(oppositeApps[it])))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            }
            context.startActivities(oppositeIntents)
        }

        clearAppsToUninstall(context)
        clearAppsToInstall(context)
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + getOverlayPackageName(packageName), true)
        } else {
            addAppToUninstall(context, getOverlayPackageName(packageName))
        }
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {
            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["samsung_oreo_icons"] = "stock_accent"
                selection["samsung_oreo_clock"] = "right"
                selection["sender_name_fix"] = "default"
                selection["notif_background"] = "white"
                selection["qs_alpha"] = "0"
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    selection["samsung_oreo_notif_style"] = "default"
                }
                return selection
            }

            override fun populateCustomizeOptions(categories: CategoryMap) {
                populateOreoCustomizeOptions(categories)
                super.populateCustomizeOptions(categories)
            }

            override fun createPreviewHandler(context: Context): PreviewHandler {
                return SamsungOreoPreviewHandler(context)
            }
        }
    }

    private class SamsungOreoPreviewHandler(context: Context) : PreviewHandler(context) {
        override fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
            super.updateView(palette, selection)
            settingsPreview?.let {
                if (selection.containsKey("samsung_oreo_clock")) {
                    val clockSelection = selection["samsung_oreo_clock"]
                    it.clock_right.setVisible(clockSelection == "right")
                    it.clock_left.setVisible(clockSelection == "left")
                    it.clock_centered.setVisible(clockSelection == "centered")
                }
            }

            val notifShadow = (selection["sender_name_fix"]) == "shadow"
            systemUiPreview?.let {
                if (notifShadow) {
                    it.preview_sysui_app_title.setShadowLayer(2.0f, -1.0f, -1.0f,
                            Color.WHITE)
                } else {
                    it.preview_sysui_app_title.setShadowLayer(0.0f, 0.0f, 0.0f,
                            Color.TRANSPARENT)
                }

                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    if (selection.containsKey("samsung_oreo_notif_style")) {
                        val darkNotif = (selection["notif_background"]) == "dark"
                        if (selection["samsung_oreo_notif_style"] == "p") {
                            it.notif_bg_layout.setImageResource(
                                    R.drawable.notif_bg_rounded)
                            if (darkNotif) {
                                it.notif_bg_layout.drawable.setTint(
                                        ColorUtils.handleColor(palette.backgroundColor, 8))
                            } else {
                                it.notif_bg_layout.drawable.setTint(
                                        context.getColor(R.color.notification_bg_light))

                            }
                        }
                    }
                }
            }
        }

        override fun updateIcons(selection: CustomizeSelection) {
            super.updateIcons(selection)

            val option = context.swift.romHandler.getCustomizeHandler()
                    .getCustomizeOptions()["samsung_oreo_icons"]!!.options[selection["samsung_oreo_icons"]]!!

            settingsIcons.forEach { icon ->
                if (option.iconTint) {
                    icon.setColorFilter(selection.accentColor)
                } else {
                    icon.clearColorFilter()
                }

                val idName =
                        "ic_${context.resources.getResourceEntryName(icon.id)}_${option.resTag}"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
            }
            systemUiIcons.forEach { icon ->
                val idName =
                        "ic_${context.resources.getResourceEntryName(icon.id)}_${option.resTag}"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
                icon.setColorFilter(selection.accentColor)
            }
        }
    }

    private fun populateOreoCustomizeOptions(categories: CategoryMap) {
            val iconOptions = OptionsMap()
            iconOptions.add(Option(context.getString(R.string.aosp_icons), "aosp", "aosp", true))
            iconOptions.add(
                    Option(context.getString(R.string.stock_icons), "stock_accent", "stock_accent", true))
            iconOptions.add(
                    Option(context.getString(R.string.stock_icons_multi), "stock_multi", "stock_multi",
                            false))
            iconOptions.add(Option(context.getString(R.string.android_p), "p", "p", false))
            categories.add(
                    CustomizeCategory(context.getString(R.string.category_icons), "samsung_oreo_icons",
                            "stock_accent", iconOptions,
                            synchronizedArrayListOf("com.android.systemui",
                                    "com.samsung.android.lool",
                                    "com.samsung.android.themestore",
                                    "com.android.settings",
                                    "com.samsung.android.app.aodservice",
                                    "android")))

            val clockOptions = OptionsMap()
            clockOptions.add(Option(context.getString(R.string.right), "right"))
            clockOptions.add(Option(context.getString(R.string.left), "left"))
            clockOptions.add(Option(context.getString(R.string.centered), "centered"))
            categories.add(
                    CustomizeCategory(context.getString(R.string.clock), "samsung_oreo_clock", "right",
                            clockOptions, synchronizedArrayListOf("com.android.systemui")))

            val notifBackgroundOptions = OptionsMap()
            notifBackgroundOptions.add(Option(context.getString(R.string.white), "white"))
            notifBackgroundOptions.add(Option(context.getString(R.string.dark), "dark"))
                notifBackgroundOptions["dark"]!!.infoText =
                        context.getString(R.string.notif_fix_desc_summary)
                val senderNameOptions = OptionsMap()
                senderNameOptions.add(Option(context.getString(R.string.disable), "default"))
                senderNameOptions.add(Option(context.getString(R.string.enable_shadow_title), "shadow"))
                notifBackgroundOptions["dark"]!!.subOptions.putAll(senderNameOptions)
                notifBackgroundOptions["dark"]!!.subOptionKey = "sender_name_fix"

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

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            val notifOptions = OptionsMap()
            notifOptions.add(Option(context.getString(R.string.default_style), "default"))
            notifOptions.add(Option(context.getString(R.string.android_p_rounded_style), "p"))
            notifOptions["p"]!!.infoDialogTitle = context.getString(R.string.rounded_dialog_title)
            notifOptions["p"]!!.infoDialogText = context.getString(R.string.rounded_dialog_info)
            categories.add(CustomizeCategory(context.getString(R.string.notification_style),
                    "samsung_oreo_notif_style", "default", notifOptions,
                    synchronizedArrayListOf("com.android.systemui", "android")))
        }
    }
}