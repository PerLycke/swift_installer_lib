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
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import com.topjohnwu.superuser.io.SuFile

open class PRomInfo(context: Context) : RomInfo(context) {

    private val systemApp = "/system/app"
    private val magiskPath = "/sbin/.core/img/swift_installer"

    private val moduleDisabled = SuFile(magiskPath, "disable").exists()
    private var useMagisk = false
    private val appPath: String

    init {
        appPath = if (!moduleDisabled && SuFile(magiskPath).exists()) {
            val f = SuFile(magiskPath, systemApp)
            if (!f.exists()) {
                f.mkdirs()
            }
            useMagisk = true
            f.absolutePath
        } else {
            useMagisk = false
            systemApp
        }
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        val overlayPackage = getOverlayPackageName(targetPackage)
        if (ShellUtils.isRootAvailable) {
            if (!useMagisk) remountRW("/system")
            ShellUtils.mkdir("$appPath/$overlayPackage")
            ShellUtils.copyFile(overlayPath, "$appPath/$overlayPackage/$overlayPackage.apk")
            ShellUtils.setPermissions(644, "$appPath/$overlayPackage/$overlayPackage.apk")
            if (!useMagisk) remountRO("/system")
        }
    }

    override fun addTutorialSteps(tutorial: TutorialActivity) {
        super.addTutorialSteps(tutorial)
        val content = tutorial.getString(R.string.magisk_module_description)
        val link = tutorial.getString(R.string.magisk_module_link_text)

        val click = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val url = context.getString(R.string.magisk_module_link)
                val builder = CustomTabsIntent.Builder()
                val intent = builder.build()
                intent.launchUrl(context, Uri.parse(url))
            }
        }
        val ss = SpannableString("$content $link")
        ss.setSpan(click, content.length + 1, content.length + 1 + link.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tutorial.addFragment(Step.Builder().setTitle("Magisk Module")
                .setContent(ss)
                .setDrawable(R.drawable.ic_magisk_logo)
                .setBackgroundColor(tutorial.getColor(R.color.background_main)).build(), TUTORIAL_PAGE_FIRST_INSTALL)
    }

    override fun getRequiredApps(): Array<String> {
        return Array(24) {
            when (it) {
                0 -> "android"
                1 -> "com.android.systemui"
                2 -> "com.amazon.clouddrive.photos"
                3 -> "com.android.settings"
                4 -> "com.anydo"
                5 -> "com.apple.android.music"
                6 -> "com.ebay.mobile"
                7 -> "com.embermitre.pixolor.app"
                8 -> "com.google.android.apps.genie.geniewidget"
                9 -> "com.google.android.apps.inbox"
                10 -> "com.google.android.apps.messaging"
                11 -> "com.google.android.gm"
                12 -> "com.google.android.talk"
                13 -> "com.mxtech.videoplayer.ad"
                14 -> "com.mxtech.videoplayer.pro"
                15 -> "com.pandora.android"
                16 -> "com.simplecity.amp.pro"
                17 -> "com.Slack"
                18 -> "com.twitter.android"
                19 -> "com.google.android.gms"
                20 -> "com.google.android.apps.nexuslauncher"
                21 -> "com.lastpass.lpandroid"
                22 -> "com.weather.Weather"
                23 -> "com.google.android.settings.intelligence"
                else -> ""
            }
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {

        if (!uninstall && oppositeApps != null && oppositeApps.isNotEmpty()) {
            for (app in oppositeApps) {
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
            if (!useMagisk) remountRW("/system")
            deleteFileRoot("$appPath/$overlayPackage/")
            if (!useMagisk) remountRO("/system")
        }
    }

    override fun isOverlayInstalled(targetPackage: String): Boolean {
        val overlayPackage = getOverlayPackageName(targetPackage)
        return SuFile("$appPath/$overlayPackage/$overlayPackage.apk").exists()
    }

    override fun getOverlayInfo(pm: PackageManager, packageName: String): PackageInfo {
        val overlayPackage = getOverlayPackageName(packageName)
        return pm.getPackageArchiveInfo("$appPath/$overlayPackage/$overlayPackage.apk", PackageManager.GET_META_DATA)
    }

    override fun getCustomizeFeatures() : Int {
        return 0
    }

    override fun magiskEnabled(): Boolean {
        return useMagisk && !moduleDisabled
    }

    override fun useHotSwap(): Boolean { return true }

    override fun getChangelogTag(): String {
        return "p"
    }

    override fun onBootCompleted(context: Context) {
        if (useMagisk && !moduleDisabled) {
            val overlays = context.assets.list("overlays") ?: emptyArray()
            remountRW("/system")
            for (packageName in overlays) {
                val opn = getOverlayPackageName(packageName)
                val systemFile = SuFile("$systemApp/$opn/$opn.apk")
                val magiskFile = SuFile("$magiskPath/${systemFile.absolutePath}")
                if (systemFile.exists()) {
                    if (!magiskFile.exists()) {
                        magiskFile.parentFile.mkdirs()
                        systemFile.renameTo(magiskFile)
                        deleteFileRoot(systemFile.parent)
                    } else {
                        val soi = context.packageManager.getPackageArchiveInfo(systemFile.absolutePath, 0)
                        val moi = context.packageManager.getPackageArchiveInfo(magiskFile.absolutePath, 0)
                        if (soi.getVersionCode() > moi.getVersionCode()) {
                            systemFile.renameTo(magiskFile)
                            deleteFileRoot(systemFile.parent)
                        }
                    }
                }
            }
            remountRO("/system")
        }
    }
}