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

package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Environment
import android.text.SpannableString
import androidx.collection.ArrayMap

object OverlayUtils {

    fun hasNightInfo(context: Context, targetPackage: String): Boolean {
        return (context.assets.list("overlays/$targetPackage") ?: arrayOf()).contains("night-mode")
    }

    fun getNightInfo(context: Context, targetPackage: String): String {
        return ShellUtils.inputStreamToString(context.assets.open("overlays/$targetPackage/night-mode"))
    }

    fun hasAppInfo(context: Context, targetPackage: String): Boolean {
        return (context.assets.list("overlays/$targetPackage") ?: arrayOf()).contains("app-info")
    }

    fun getAppInfo(context: Context, targetPackage: String): String {
        return ShellUtils.inputStreamToString(context.assets.open("overlays/$targetPackage/app-info"))
    }

    fun getTargetPackage(packageName: String): String {
        return if (packageName.endsWith(".swiftinstaller.overlay")) {
            packageName.substring(0, packageName.lastIndexOf(".swiftinstaller.overlay"))
        } else {
            packageName
        }
    }

    fun getOverlayVersion(context: Context, targetPackage: String): Long {
        return try {
            Integer.parseInt(ShellUtils.inputStreamToString(context.assets.open(
                    "overlays/$targetPackage/version")).trim().replace("\"", "")).toLong()
        } catch (e: Exception) {
            return 0
        }
    }

    fun wasUpdateSuccessful(context: Context, packageName: String): Boolean {
        if (!context.swift.romHandler.isOverlayInstalled(packageName)) return false
        if (!context.pm.isAppInstalled(packageName)) return false
        val appVersion = context.packageManager.getPackageInfo(packageName, 0).getVersionCode()
        val overlayAppVersion = context.swift.romHandler.getOverlayInfo(
                context.packageManager, packageName).applicationInfo.metaData
                .getInt("app_version_code").toLong()
        val overlayVersion = getOverlayVersion(context, packageName)
        val curOverlayVersion =
                context.swift.romHandler.getOverlayInfo(context.packageManager, packageName)
                        .getVersionCode()
        return (appVersion == overlayAppVersion) && (overlayVersion == curOverlayVersion)
    }

    fun checkAppVersion(context: Context, packageName: String): Boolean {
        if (!context.swift.romHandler.isOverlayInstalled(packageName)) return false
        val appVersionCode = context.packageManager.getPackageInfo(packageName, 0).getVersionCode()
        val curVersionCode =
                context.swift.romHandler.getOverlayInfo(context.packageManager, packageName)
                        .applicationInfo.metaData.getInt("app_version_code")
        return appVersionCode > curVersionCode
    }

    fun checkOverlayVersion(context: Context, packageName: String): Boolean {
        if (!context.swift.romHandler.isOverlayInstalled(packageName)) return false
        val overlayVersion = getOverlayVersion(context, packageName)
        val currentVersion =
                context.swift.romHandler.getOverlayInfo(context.packageManager, packageName)
                        .getVersionCode()
        return overlayVersion > currentVersion
    }

    fun getOverlayPackageName(pack: String): String {
        return "$pack.swiftinstaller.overlay"
    }

    fun getOverlayPath(packageName: String): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/.swift/" +
                "/overlays/compiled/" + getOverlayPackageName(packageName) + ".apk"
    }

    fun isOverlayEnabled(packageName: String): Boolean {
        if (!Utils.isSamsungOreo()) {
            val overlays = runCommand("cmd overlay list", true).output
            for (overlay in overlays!!.split("\n")) {
                if (overlay.startsWith("[x]") && overlay.contains(packageName)) {
                    return true
                }
            }
        }
        return Utils.isSamsungOreo()
    }

    fun overlayHasVersion(context: Context, packageName: String): Boolean {
        val array = context.assets.list("overlays/$packageName") ?: emptyArray()
        return array.contains("versions")
    }

    fun checkVersionCompatible(context: Context, packageName: String): Boolean {
        val packageInfo = try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: java.lang.Exception) {
            return false
        }
        val array = context.assets.list("overlays/$packageName") ?: emptyArray()
        if (array.contains("versions")) {
            val vers = context.assets.list("overlays/$packageName/versions") ?: emptyArray()
            for (ver in vers) {
                if (packageInfo.versionName.startsWith(ver)) {
                    return true
                }
            }
        } else {
            return true
        }
        return false
    }

    fun getApkLink (context : Context, m: CharSequence, packageInfo: PackageInfo, packageName: String): CharSequence {
        if (overlayHasVersion(context, packageName)) {
            val vers = context.assets.list("overlays/$packageName/versions") ?: emptyArray()
            var ss = SpannableString(m)
            for (line in m.split("\n")) {
                for (ver in vers) {
                    if (line.contains(ver) && line.contains("Download")) {
                        val start = m.indexOf(line) + line.split(":")[0].length + 2
                        val end = start + "Download".length
                        if ((context.assets.list("overlays/$packageName") ?: emptyArray()).contains("apk-link-$ver")) {
                            ss = Utils.createLinkedString(context, ss, start, end, ShellUtils.inputStreamToString(context.assets.open("overlays/$packageName/apk-link-$ver")))
                        }
                    }
                }
                if (line.contains("Download latest supported apk")) {
                    val start = m.indexOf(line)
                    val end = start + "Download latest supported apk".length
                    ss = Utils.createLinkedString(context, ss, start, end, ShellUtils.inputStreamToString(context.assets.open("overlays/$packageName/apk-link-all")))
                }
            }
            return ss
        }
        return m
    }

    fun getAvailableOverlayVersions(context: Context, packageInfo: PackageInfo, packageName: String): CharSequence {
        val versions = StringBuilder()
        val vers = context.assets.list("overlays/$packageName/versions") ?: emptyArray()
        for (version in vers) {
            if (version != "common") {
                if (packageInfo.versionName.startsWith(version)) {
                    versions.append("<font color='#47AE84'><b>$version.x</b></font><br>")
                } else {
                    if ((context.assets.list("overlays/$packageName")
                                    ?: arrayOf()).contains("apk-link-$version")) {
                        versions.append("<font color='#FF6868'>$version.x: </font>Download<br>")
                    } else {
                        versions.append("<font color='#FF6868'>$version.x</font><br>")
                    }
                }
            }
        }
        if ((context.assets.list("overlays/$packageName")
                        ?: arrayOf()).contains("apk-link-all")) {
            versions.append("<br>Download latest supported apk<br>")
        }
        return versions.substring(0, versions.length - 2)
    }

    fun getOverlayOptions(context: Context, packageName: String): ArrayMap<String, Array<String>> {
        val optionsMap = ArrayMap<String, Array<String>>()
        val options = context.assets.list("overlays/$packageName/options") ?: emptyArray()
        for (option in options) {
            val array = context.assets.list("overlays/$packageName/options/$option") ?: emptyArray()
            if (array.isNotEmpty()) {
                optionsMap[option] = array
            }
        }
        return optionsMap
    }

    fun checkAndHideOverlays(context: Context) {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        val extras = context.swift.extrasHandler.appExtras.keys
        for (overlay in overlays.plus(extras)) {
            if (checkOverlay(context, overlay)) {
                addHiddenApp(context, overlay)
            } else {
                removeHiddenApp(context, overlay)
            }
        }
    }

    fun enableAllOverlays(): Boolean {
        var hasEnabledOverlays = false
        val overlays = runCommand("cmd overlay list", true).output
        for (overlay in overlays!!.split("\n")) {
            if (overlay.startsWith("[")) {
                val pn = overlay.split("]")[1].trim()
                if (overlay.startsWith("[ ]")) {
                    if (pn.endsWith(".swiftinstaller.overlay")) {
                        runCommand("cmd overlay enable $pn", true)
                        hasEnabledOverlays = true
                    }
                }
            }
        }
        return hasEnabledOverlays
    }

    fun hasOverlay(context: Context, packageName: String): Boolean {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        return overlays.contains(packageName)
    }

    fun parseOverlayResourcePath(context: Context, path: String, packageName: String,
                                 resourcePaths: SynchronizedArrayList<String>) {
        val am = context.assets
        val variants = am.list(path) ?: return
        if (variants.contains("common")) {
            checkResourcePath(context, "$path/common", packageName, resourcePaths)
        }
        if (variants.contains("versions")) {
            parseOverlayVersions(context, packageName, resourcePaths, "$path/versions")
        }
        if (variants.contains("props")) {
            val props = am.list("$path/props")
            if (props != null) {
                var found = false
                for (prop in props) {
                    if (getProperty(prop, "prop") != "prop") {
                        found = true
                        val propVal = getProperty(prop) ?: "default"
                        val vals = am.list("$path/props/$prop") ?: continue
                        if (vals.contains("common")) {
                            checkResourcePath(context, "$path/props/$prop/common", packageName,
                                    resourcePaths)
                        }
                        for (`val` in vals) {
                            if (`val` == propVal || `val`.startsWith(propVal)) {
                                checkResourcePath(context, "$path/props/$prop/$propVal",
                                        packageName, resourcePaths)
                            }
                        }
                    }
                }
                if (!found) {
                    if (props.contains("default")) {
                        checkResourcePath(context, "$path/props/default", packageName,
                                resourcePaths)
                    }
                }
            }
        }
        if (variants.contains("options")) {
            val optionsMap = getSelectedOverlayOptions(context, packageName)
            val options = context.assets.list("$path/options") ?: emptyArray()
            applyDefaultOptionsToMap(context, optionsMap, path)
            for (option in options) {
                if (optionsMap.containsKey(option)) {
                    val optionsArray = context.assets.list("$path/options/$option") ?: emptyArray()
                    if (optionsArray.isNotEmpty()) {
                        checkResourcePath(context, "$path/options/$option/${optionsMap[option]}",
                                packageName, resourcePaths)
                    }
                }
            }
        }
        if (variants.contains("customize")) {
            val cHandler = context.swift.romHandler.getCustomizeHandler()
            val list = context.assets.list("$path/customize") ?: emptyArray()
            for (cust in list) {
                if (cHandler.getCustomizeOptions().containsKey(cust)) {
                    val selection = cHandler.getSelection()[cust]
                    val cList = context.assets.list("$path/customize/$cust") ?: emptyArray()
                    if (cList.contains(selection)) {
                        checkResourcePath(context, "$path/customize/$cust/$selection", packageName,
                                resourcePaths)
                    }
                }
            }
        }
    }

    private fun applyDefaultOptionsToMap(context: Context, optionsMap: ArrayMap<String, String>, path: String) {
        val options = context.assets.list("$path/options") ?: emptyArray()
        for (option in options) {
            val opt = context.assets.list("$path/options/$option")?: emptyArray()
            if (opt.contains("off")) {
                optionsMap.putIfAbsent(option, "off")
            } else {
                optionsMap.putIfAbsent(option, opt[0])
            }
        }
    }

    private fun checkOverlay(context: Context, packageName: String): Boolean {
        val variants = context.assets.list("overlays/$packageName") ?: emptyArray()
        if (variants.contains("versions")) return false

        val resourcePaths = SynchronizedArrayList<String>()
        val assets = SynchronizedArrayList<String>()
        parseOverlayResourcePath(context, "overlays/$packageName", packageName, resourcePaths)
        for (path in resourcePaths) {
            val list = context.assets.list(path) ?: emptyArray()
            if (list.isNotEmpty()) {
                for (l in list) {
                    assets.add(l)
                }
            }
        }
        return assets.isEmpty()
    }

    private fun checkResourcePath(context: Context, path: String, packageName: String,
                                  resourcePaths: SynchronizedArrayList<String>) {
        val variants = context.assets.list(path) ?: return
        if (!variants.contains("props")
                && !variants.contains("versions") && !variants.contains("common")) {
            addResourcePath(resourcePaths, path)
        } else {
            parseOverlayResourcePath(context, path, packageName, resourcePaths)
        }
    }

    private fun addResourcePath(resourcePaths: SynchronizedArrayList<String>, path: String) {
        resourcePaths.add(path.trimEnd('/'))
    }

    fun parseOverlayAssetPath(am: AssetManager, path: String, assetPaths: SynchronizedArrayList<String>) {
        val variants = am.list("$path/assets") ?: return
        if (variants.contains("common")) {
            assetPaths.add("$path/assets/common")
        }
    }

    private fun parseOverlayVersions(context: Context, packageName: String,
                                     resourcePaths: SynchronizedArrayList<String>, path: String) {
        val vers = context.assets.list(path) ?: return
        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            if (vers.contains("common")) {
                checkResourcePath(context, "$path/common", packageName, resourcePaths)
            }
            for (ver in vers) {
                if (packageInfo.versionName.startsWith(ver)) {
                    checkResourcePath(context, "$path/$ver", packageName, resourcePaths)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // ignore
        }
    }
}