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
import android.net.Uri
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
import java.io.File

class SamsungPRomHandler(context: Context) : RomHandler(context) {

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
                    "com.android.emergency",
                    "com.android.bluetooth",
                    "com.android.documentsui",
                    "com.android.phone",
                    "com.android.settings",
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
                    "com.samsung.android.contacts",
                    "com.samsung.android.da.daagent",
                    "com.samsung.android.email.provider",
                    "com.samsung.android.game.gametools",
                    "com.samsung.android.gametuner.thin",
                    "com.samsung.android.incallui",
                    "com.samsung.android.lool",
                    "com.samsung.android.messaging",
                    "com.samsung.android.oneconnect",
                    "com.samsung.android.samsungpassautofill",
                    "com.samsung.android.securitylogagent",
                    "com.samsung.android.themestore",
                    "com.samsung.android.videolist",
                    "com.samsung.app.newtrim",
                    "com.samsung.networkui",
                    "com.sec.android.app.clockpackage",
                    "com.sec.android.app.launcher",
                    "com.sec.android.app.music",
                    "com.sec.android.app.myfiles",
                    "com.sec.android.app.popupcalculator",
                    "com.sec.android.app.samsungapps",
                    "com.sec.android.app.sbrowser",
                    "com.sec.android.app.sbrowser.beta",
                    "com.sec.android.app.simsettingmgr",
                    "com.sec.android.app.soundalive",
                    "com.sec.android.app.voicenote",
                    "com.sec.android.daemonapp",
                    "com.sec.android.gallery3d",
                    "com.sec.android.inputmethod",
                    "com.sec.hearingadjust",
                    "com.android.server.telecom",
                    "com.google.android.packageinstaller",
                    "com.wssyncmldm"
            )
    }

    override fun getRequiredApps(): Array<String> {
            return arrayOf()
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


            override fun createPreviewHandler(context: Context): PreviewHandler {
                return SamsungPiePreviewHandler(context)
            }
        }
    }

    private class SamsungPiePreviewHandler(context: Context) : PreviewHandler(context) {

        override fun updateIcons(selection: CustomizeSelection) {
            super.updateIcons(selection)

            settingsIcons.forEach { icon ->

                val idName =
                        "ic_${context.resources.getResourceEntryName(icon.id)}_p"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
            }
            systemUiIcons.forEach { icon ->
                val idName =
                        "ic_${context.resources.getResourceEntryName(icon.id)}_p"
                val id = context.resources.getIdentifier("${context.packageName}:drawable/$idName",
                        null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
                icon.setColorFilter(selection.accentColor)
            }
        }
    }
}