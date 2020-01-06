/*
 *
 *  * Copyright (C) 2019 Griffin Millender
 *  * Copyright (C) 2019 Per Lycke
 *  * Copyright (C) 2019 Davide Lilli & Nishith Khanna
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
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.*
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPath
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*
import java.io.File
import java.net.URLConnection

class SamsungQRomHandler(context: Context) : RomHandler(context) {

    private val qHandler = QRomHandler(context)

    override fun requiresRoot(): Boolean {
        return false
    }

    override fun getChangelogTag(): String {
        return "samsung-q"
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
                    "com.android.emergency",
                    "com.android.bluetooth",
                    "com.android.phone",
                    "com.samsung.android.app.aodservice",
                    "com.samsung.android.app.appsedge",
                    "com.samsung.android.app.cocktailbarservice",
                    "com.samsung.android.app.notes",
                    "com.samsung.android.app.smartcapture",
                    "com.samsung.android.app.spage",
                    "com.samsung.android.applock",
                    "com.samsung.android.bixby.agent",
                    "com.samsung.android.calendar",
                    "com.samsung.android.clipboarduiservice",
                    "com.samsung.android.da.daagent",
                    "com.samsung.android.game.gametools",
                    "com.samsung.android.gametuner.thin",
                    "com.samsung.android.oneconnect",
                    "com.samsung.android.samsungpassautofill",
                    "com.samsung.android.securitylogagent",
                    "com.samsung.android.videolist",
                    "com.samsung.app.newtrim",
                    "com.samsung.networkui",
                    "com.sec.android.app.clockpackage",
                    "com.sec.android.app.launcher",
                    "com.sec.android.app.myfiles",
                    "com.sec.android.app.simsettingmgr",
                    "com.sec.android.app.soundalive",
                    "com.sec.android.app.voicenote",
                    "com.sec.android.daemonapp",
                    "com.sec.android.gallery3d",
                    "com.sec.hearingadjust",
                    "com.android.server.telecom",
                    "com.wssyncmldm",
                    "com.samsung.android.scloud",
                    "com.android.providers.media",
                    "com.google.android.marvin.talkback"
            )
    }

    override fun getRequiredApps(): Array<String> {
            return arrayOf(
                    "android",
                    "com.android.systemui"
            )
    }

    override fun getDefaultAccent(): Int {
        return ColorUtils.convertToColorInt("3a99ff")
    }

    override fun postInstall(uninstall: Boolean, apps: SynchronizedArrayList<String>,
                             oppositeApps: SynchronizedArrayList<String>, intent: Intent?) {
        if (ShellUtils.isRootAvailable) {
            qHandler.postInstall(uninstall, apps, oppositeApps, intent)
            if (apps.contains("android") || apps.contains("com.android.systemui")) {
                runCommand("settings put system current_sec_active_themepackage 1", true)
                runCommand("settings put system current_theme_support_night_mode 1", true)

            }
            return
        }

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
        
        if (Utils.isSynergyInstalled(context, "projekt.samsung.theme.compiler") && Utils.isSynergyCompatibleDevice()) {
                if (apps.size == 1) {
                    val file = File(getOverlayPath(apps[0]))
                    val fileUri = FileProvider.getUriForFile(context,
                            "${context.packageName}.myprovider", file)
                    Intent(Intent.ACTION_SEND).run {
                        `package` = "projekt.samsung.theme.compiler"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        type = URLConnection.guessContentTypeFromName(file.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(this)
                    }
                } else if (apps.size > 1) {
                    val fileExtras = arrayListOf<Parcelable>()
                    apps.forEach { app ->
                        val fileUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.myprovider",
                                File(getOverlayPath(app))
                        )
                        fileExtras.add(fileUri)
                    }
                    Intent(Intent.ACTION_SEND_MULTIPLE).run {
                        `package` = "projekt.samsung.theme.compiler"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileExtras)
                        type = URLConnection.guessContentTypeFromName(File(getOverlayPath(apps[0])).name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(this)
                    }
                }
            }

        clearAppsToUninstall(context)
        clearAppsToInstall(context)
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            qHandler.installOverlay(context, targetPackage, overlayPath)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            qHandler.uninstallOverlay(context, packageName)
        } else {
            addAppToUninstall(context, getOverlayPackageName(packageName))
        }
    }

    override fun isOverlayInstalled(targetPackage: String): Boolean {
        if (ShellUtils.isRootAvailable) {
            return qHandler.isOverlayInstalled(targetPackage)
        }
        return super.isOverlayInstalled(targetPackage)
    }

    override fun getOverlayInfo(pm: PackageManager, packageName: String): PackageInfo {
        if (ShellUtils.isRootAvailable) {
            return qHandler.getOverlayInfo(pm, packageName)
        }
        return super.getOverlayInfo(pm, packageName)
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {
            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["qs_alpha"] = "0"
                selection["sbar_icons_color"] = "grey"
                selection["qs_icons_color_samsung_pie"] = "accent"
                selection["settings_icons_color_samsung_pie"] = "multi"
                selection["settings_icons_samsung_pie"] = "stock"
                return selection
            }

            override fun populateCustomizeOptions(categories: CategoryMap) {
                populateQCustomizeOptions(categories)
                super.populateCustomizeOptions(categories)
            }


            override fun createPreviewHandler(context: Context): PreviewHandler {
                return SamsungQPreviewHandler(context)
            }
        }
    }

    private class SamsungQPreviewHandler(context: Context) : PreviewHandler(context) {

        override fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
            super.updateView(palette, selection)
            systemUiPreview?.let {
                it.notif_bg_layout.setImageResource(R.drawable.notif_bg_rounded)
                it.notif_bg_layout.drawable.setTint(context.getColor(R.color.oneui_notification_color))
                it.preview_sysui_sender.text =
                        context.getString(R.string.dark_notifications)
                it.preview_sysui_sender.setTextColor(context.getColor(R.color.white))
                it.preview_sysui_msg.setTextColor(Color.parseColor("#b3ffffff"))
            }

        }

        override fun updateIcons(selection: CustomizeSelection) {
            super.updateIcons(selection)

            settingsIcons.forEach { icon ->
                    val idName =
                            "ic_${context.resources.getResourceEntryName(icon.id)}_stock_multi"
                    val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                            null, null)
                    if (id > 0) {
                        icon.setImageDrawable(context.getDrawable(id))
                    }
                }
                val qsIcon = (selection["qs_icons_color_samsung_pie"]) == "accent"
                systemUiIcons.forEach { icon ->
                    val idName =
                            "ic_${context.resources.getResourceEntryName(icon.id)}_aosp"
                    val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                            null, null)
                    if (id > 0) {
                        val drawable = context.getDrawable(id)?.mutate() as LayerDrawable
                        if (qsIcon) {
                            drawable.findDrawableByLayerId(R.id.icon_bg)
                                    .setTint(selection.accentColor)
                        } else {
                            drawable.findDrawableByLayerId(R.id.icon_bg)
                                    .setTint(context.getColor(R.color.white))
                        }
                        drawable.findDrawableByLayerId(
                                R.id.icon_tint).setTint(selection.backgroundColor)
                        icon.setImageDrawable(drawable)
                    }
                }
            }
        }

    private fun populateQCustomizeOptions(categories: CategoryMap) {

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