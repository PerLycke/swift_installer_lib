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

package com.brit.swiftinstaller.library.installer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Environment
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.ui.customize.CustomizeSelection
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.ColorUtils.getAlphaDimen
import com.brit.swiftinstaller.library.utils.ColorUtils.toHexString
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.NO_PERMISSION_PACKAGES
import com.brit.swiftinstaller.library.utils.OverlayUtils
import com.brit.swiftinstaller.library.utils.OverlayUtils.checkVersionCompatible
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPath
import com.brit.swiftinstaller.library.utils.ShellUtils
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.deleteFileShell
import com.brit.swiftinstaller.library.utils.extractAsset
import com.brit.swiftinstaller.library.utils.getVersionCode
import com.brit.swiftinstaller.library.utils.swift
import com.brit.swiftinstaller.library.utils.useBackgroundPalette
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class OverlayTask(private val om: OverlayManager) : Runnable {

    lateinit var context: Context

    lateinit var selection: CustomizeSelection

    lateinit var packageName: String
    private lateinit var packageInfo: PackageInfo
    private lateinit var appInfo: ApplicationInfo
    private lateinit var resDir: File
    private lateinit var assetDir: File
    private lateinit var overlayDir: File
    private lateinit var overlayPath: String
    var errorLog = ""
    var uninstall: Boolean = false
    var index = 0

    fun initializeOverlayTask(context: Context, packageName: String, index: Int,
                              uninstall: Boolean) {
        this.context = context
        this.selection = context.swift.romHandler.getCustomizeHandler().getSelection()
        this.packageName = packageName
        this.packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        this.appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        this.overlayDir =
                File(Environment.getExternalStorageDirectory(), ".swift/overlays/$packageName")
        this.resDir = File(overlayDir, "res")
        this.assetDir = File(overlayDir, "assets")
        if (resDir.exists())
            resDir.deleteRecursively()
        if (assetDir.exists())
            assetDir.deleteRecursively()
        resDir.mkdirs()
        assetDir.mkdirs()
        this.overlayPath = getOverlayPath(packageName)
        if (!File(overlayPath).parentFile.exists())
            File(overlayPath).parentFile.mkdirs()
        if (File(overlayPath).exists())
            File(overlayPath).delete()
        this.index = index
        this.uninstall = uninstall
    }

    fun getRunnable(): Runnable {
        return this
    }

    override fun run() {
        if (uninstall) {
            context.swift.romHandler.uninstallOverlay(context, packageName)
            om.handleState(this, OverlayManager.OVERLAY_UNINSTALLED)
        } else {
            if (!checkVersionCompatible(context, packageName)) {
                errorLog = "Version Incompatible"
                om.handleState(this, OverlayManager.OVERLAY_FAILED)
            } else {
                extractResources()
                compileOverlay()
                deleteFileShell(overlayDir.absolutePath)
            }
            om.handleState(this, OverlayManager.OVERLAY_INSTALLED)
        }
    }

    private fun extractResources() {
        if (resDir.exists())
            deleteFileShell(resDir.absolutePath)

        resDir.mkdirs()

        val am = context.assets
        val resourcePaths = SynchronizedArrayList<String>()
        val assetPaths = SynchronizedArrayList<String>()
        OverlayUtils.parseOverlayResourcePath(context, "overlays/$packageName", packageName,
                resourcePaths)
        OverlayUtils.parseOverlayAssetPath(am, "overlays/$packageName", assetPaths)
        resourcePaths.forEach { path ->
            am.extractAsset(path, resDir.absolutePath, context.swift.cipher)
        }
        assetPaths.forEach { path ->
            am.extractAsset(path, assetDir.absolutePath, context.swift.cipher)
        }
        if (context.swift.extrasHandler.resourceExtras.contains(packageName)) {
            context.swift.extrasHandler.resourceExtras[packageName]?.invoke(context, resDir.absolutePath, assetDir.absolutePath)
        }
        if (packageName == "android") {
            applyAccent()
            applyBackground()
        }
        if (File(resDir, "version").exists()) {
            deleteFileShell("$resDir/version")
        }
        generateManifest(overlayDir.absolutePath, packageName, packageInfo.versionName,
                packageInfo.getVersionCode(), OverlayUtils.getOverlayVersion(context, packageName))
    }

    private fun compileOverlay() {
        var assets: String? = null
        if (assetDir.exists() && assetDir.isDirectory && !assetDir.list().isEmpty()) {
            assets = assetDir.absolutePath
        }
        val output =
                ShellUtils.compileOverlay(context, BuildConfig.APPLICATION_ID, resDir.absolutePath,
                        overlayDir.absolutePath + "/AndroidManifest.xml",
                        overlayPath, assets, appInfo)
        if (output.exitCode == 0) {
            context.swift.romHandler.installOverlay(context, packageName, overlayPath)
        } else {
            errorLog = output.output ?: ""
            om.handleState(this, OverlayManager.OVERLAY_FAILED)
        }
    }

    private fun applyAccent() {
        val accent = selection.accentColor
        val darkAccent = ColorUtils.handleColor(accent, -40)
        val lightAccent = ColorUtils.handleColor(accent, 40)
        val file = StringBuilder()
        file.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        file.append("<resources>\n")
        file.append(
                "<color name=\"material_blue_grey_900\">#${String.format("%06x", accent).substring(
                        2)}</color>\n")
        file.append(
                "<color name=\"material_blue_grey_800\">#${String.format("%06x", darkAccent).substring(
                        2)}</color>\n")
        file.append(
                "<color name=\"material_blue_grey_700\">#${String.format("%06x", lightAccent).substring(
                        2)}</color>\n")
        file.append("<color name=\"highlighted_text_dark\">#${ColorUtils.getAlpha(accent, 30)}</color>\n")
        file.append("</resources>")

        val values = File(resDir, "/values")
        values.mkdirs()
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter(resDir.absolutePath + "/values/accent.xml"))
            writer.write(file.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun applyBackground() {
        val palette = MaterialPalette.createPalette(selection.backgroundColor,
                useBackgroundPalette(context))
        val file = StringBuilder()
        file.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        file.append("<resources>\n")
        file.append("<color name=\"background_material_dark\">#${toHexString(
                palette.backgroundColor)}</color>\n")
        file.append("<color name=\"background_floating_material_dark\">#${toHexString(
                palette.floatingBackground)}</color>\n")
        file.append("<color name=\"button_material_dark\">#${toHexString(
                palette.buttonBackground)}</color>\n")
        file.append("<color name=\"legacy_primary\">#${toHexString(
                palette.darkBackgroundColor)}</color>\n")
        file.append("<color name=\"legacy_green\">#${toHexString(palette.cardBackground)}</color>\n")
        file.append(
                "<color name=\"legacy_orange\">#${toHexString(palette.otherBackground)}</color>\n")
        file.append("<color name=\"legacy_control_activated\">#${ColorUtils.getAlpha(
                palette.darkBackgroundColor, selection.getInt("qs_alpha"))}</color>\n")
        file.append(
                "<item type=\"dimen\" name=\"disabled_alpha_leanback_formwizard\">${getAlphaDimen(
                        selection.getInt("qs_alpha"))}</item>\n")
        file.append("</resources>")

        val values = File(resDir, "/values")
        values.mkdirs()
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter(resDir.absolutePath + "/values/background.xml"))
            writer.write(file.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun generateManifest(path: String, targetPackage: String,
                                 appVersion: String, appVersionCode: Long, overlayVersion: Long) {
        val manifest = StringBuilder()
        manifest.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n")
        manifest.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        manifest.append("package=\"${getOverlayPackageName(targetPackage)}\"\n")
        manifest.append("android:versionCode=\"$overlayVersion\"\n")
        manifest.append("android:versionName=\"$overlayVersion\">\n")
        if (!NO_PERMISSION_PACKAGES.contains(targetPackage) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manifest.append(
                    "<uses-permission android:name=\"com.samsung.android.permission.SAMSUNG_OVERLAY_COMPONENT\" />\n")
        }
        manifest.append("<overlay android:targetPackage=\"$targetPackage\"/>\n")
        manifest.append("<application android:allowBackup=\"false\" android:hasCode=\"false\">\n")
        manifest.append(
                "<meta-data android:name=\"app_version\" android:value=\"v=$appVersion\"/>\n")
        manifest.append(
                "<meta-data android:name=\"app_version_code\" android:value=\"$appVersionCode\"/>\n")
        manifest.append(
                "<meta-data android:name=\"overlay_version\" android:value=\"$overlayVersion\"/>\n")
        manifest.append(
                "<meta-data android:name=\"target_package\" android:value=\"$targetPackage\"/>\n")
        manifest.append("</application>\n")
        manifest.append("</manifest>")

        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter("$path/AndroidManifest.xml"))
            writer.write(manifest.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}