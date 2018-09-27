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

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.CustomizeHandler
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.getProperty
import com.brit.swiftinstaller.library.utils.isAppInstalled
import com.brit.swiftinstaller.library.utils.pm
import com.brit.swiftinstaller.library.utils.synchronizedArrayListOf
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity

@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
abstract class RomInfo constructor(var context: Context) {

    private var customizeHandler: CustomizeHandler? = null

    open fun getDefaultAccent(): Int {
        return context.getColor(R.color.minimal_green)
    }

    open fun getDisabledOverlays(): SynchronizedArrayList<String> {
        return SynchronizedArrayList()
    }

    open fun getRequiredApps(): Array<String> {
        return emptyArray()
    }

    open fun addTutorialSteps(tutorial: TutorialActivity) {
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.swift_app_name))
                .setContent(tutorial.getString(R.string.tutorial_guide))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_logo) // int top drawable
                .build(), TUTORIAL_PAGE_MAIN)
        tutorial.addFragment(
                Step.Builder().setTitle(tutorial.getString(R.string.tutorial_apps_title))
                        .setContent(tutorial.getString(R.string.tutorial_apps))
                        .setBackgroundColor(
                                ContextCompat.getColor(tutorial, R.color.background_main))
                        .setDrawable(R.drawable.ic_apps) // int top drawable
                        .build(), TUTORIAL_PAGE_APPS)
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.basic_usage))
                .setContent(tutorial.getString(R.string.tutorial_basic_usage_content))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_hand) // int top drawable
                .build(), TUTORIAL_PAGE_USAGE)
        tutorial.addFragment(PermissionStep.Builder().setTitle(
                tutorial.getString(R.string.tutorial_permission_title))
                .setContent(tutorial.getString(R.string.tutorial_permission_content))
                .setBackgroundColor(ContextCompat.getColor(tutorial,
                        R.color.background_main)) // int background color
                .setDrawable(R.drawable.ic_tutorial_permission)
                .setPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .build(), TUTORIAL_PAGE_PERMISSIONS)
        tutorial.addFragment(
                Step.Builder().setTitle(tutorial.getString(R.string.tutorial_customize_title))
                        .setContent(tutorial.getString(R.string.tutorial_customize_content))
                        .setBackgroundColor(
                                ContextCompat.getColor(tutorial, R.color.background_main))
                        .setDrawable(R.drawable.ic_tutorial_customize) // int top drawable
                        .build(), TUTORIAL_PAGE_PERSONALIZE)
    }

    open fun isOverlayInstalled(targetPackage: String): Boolean {
        return context.pm.isAppInstalled(getOverlayPackageName(targetPackage))
    }

    open fun disableOverlay(targetPackage: String) {
    }

    open fun requiresRoot(): Boolean {
        return true
    }

    fun getCustomizeHandler(): CustomizeHandler {
        if (customizeHandler == null) {
            customizeHandler = createCustomizeHandler()
        }
        return customizeHandler!!
    }

    open fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {

        }
    }

    open fun useHotSwap(): Boolean {
        return false
    }

    open fun neverReboot(): Boolean {
        return false
    }

    abstract fun installOverlay(context: Context, targetPackage: String, overlayPath: String)
    abstract fun postInstall(uninstall: Boolean = false,
                             apps: SynchronizedArrayList<String> = synchronizedArrayListOf(),
                             oppositeApps: SynchronizedArrayList<String> = synchronizedArrayListOf(),
                             intent: Intent? = null)

    abstract fun uninstallOverlay(context: Context, packageName: String)
    abstract fun getChangelogTag(): String

    open fun getOverlayInfo(pm: PackageManager, packageName: String): PackageInfo {
        return pm.getPackageInfo(getOverlayPackageName(packageName), PackageManager.GET_META_DATA)
    }

    open fun magiskEnabled(): Boolean {
        return false
    }

    open fun onBootCompleted(context: Context) {
    }

    companion object {

        const val TUTORIAL_PAGE_MAIN = 0
        const val TUTORIAL_PAGE_APPS = 1
        const val TUTORIAL_PAGE_USAGE = 2
        const val TUTORIAL_PAGE_PERMISSIONS = 3
        const val TUTORIAL_PAGE_PERSONALIZE = 4

        @Synchronized
        @JvmStatic
        fun createRomInfo(context: Context): RomInfo {
            return when {
                getProperty("ro.oxygen.version", "def") != "def"
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> OOSPRomInfo(context)
                getProperty("ro.oxygen.version", "def") != "def"
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> OOSOreoRomInfo(context)
                getProperty("ro.config.knox", "def") != "def" -> SamsungRomInfo(context)
                Build.VERSION_CODES.P == Build.VERSION.SDK_INT -> PRomInfo(context)
                else -> OreoRomInfo(context)
            }
        }

        @Suppress("DEPRECATION", "unused")
        private fun isOMS(context: Context): Boolean {
            val am = context.getSystemService(ActivityManager::class.java)!!
            val services = am.getRunningServices(Integer.MAX_VALUE)
            for (info in services) {
                if (info.service.className.contains("IOverlayManager")) {
                    return true
                }
            }
            return false
        }
    }
}
