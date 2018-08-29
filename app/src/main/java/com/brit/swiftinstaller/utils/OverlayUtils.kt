package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import androidx.collection.ArrayMap
import com.brit.swiftinstaller.installer.rom.RomInfo
import java.util.*

object OverlayUtils {

    fun isSwiftOverlay(packageName: String) : Boolean {
        return packageName.endsWith(".swiftinstaller.overlay")
    }

    fun getOverlayVersion(context: Context, targetPackage: String): Long {
        return Integer.parseInt(ShellUtils.inputStreamToString(context.assets.open(
                "overlays/$targetPackage/version")).trim().replace("\"", "")).toLong()
    }

    fun checkOverlayVersion(context: Context, packageName: String): Boolean {
        if (!Utils.isAppInstalled(context, getOverlayPackageName(packageName))) return false
        val overlayVersion = getOverlayVersion(context, packageName)
        val currentVersion = context.packageManager.getPackageInfo(
                getOverlayPackageName(packageName), 0).getVersionCode()
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
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
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

    fun getAvailableOverlayVersions(context: Context, packageName: String): String {
        val versions = StringBuilder()
        val vers = context.assets.list("overlays/$packageName/versions") ?: emptyArray()
        for (version in vers) {
            if (version != "common") {
                versions.append("v$version, ")
            }
        }
        return versions.substring(0, versions.length - 2)
    }

    fun getInstalledOverlays(context: Context): ArrayList<String> {
        val apps = ArrayList<String>()
        val overlays = context.assets.list("overlays") ?: emptyArray()
        for (app in overlays) {
            if (RomInfo.getRomInfo(context).isOverlayInstalled(app)) {
                apps.add(app)
            }
        }
        return apps
    }

    fun getOverlayOptions(context: Context, packageName: String) : ArrayMap<String, Array<String>> {
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
        for (overlay in overlays) {
            if (checkOverlay(context, overlay)) {
                addHiddenApp(context, overlay)
            } else {
                removeHiddenApp(context, overlay)
            }
        }
    }

    fun hasOverlay(context: Context, packageName: String): Boolean {
        val overlays = context.assets.list("overlays") ?: emptyArray()
        return overlays.contains(packageName)
    }

    fun parseOverlayResourcePath(context: Context, path: String, packageName: String, resourcePaths: ArrayList<String>) {
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
                    Log.d("TEST", "prop - $prop")
                    Log.d("TEST", "value - ${getProperty(prop, "prop")}")
                    if (getProperty(prop, "prop") != "prop") {
                        found = true
                        val propVal = getProperty(prop) ?: "default"
                        val vals = am.list("$path/props/$prop") ?: continue
                        if (vals.contains("common")) {
                            checkResourcePath(context, "$path/props/$prop/common", packageName, resourcePaths)
                        }
                        for (`val` in vals) {
                            if (`val` == propVal || `val`.startsWith(propVal)) {
                                checkResourcePath(context, "$path/props/$prop/$propVal", packageName, resourcePaths)
                            }
                        }
                    }
                }
                if (!found) {
                    if (props.contains("default")) {
                        checkResourcePath(context, "$path/props/default", packageName, resourcePaths)
                    }
                }
            }
        }
        if (variants.contains("options")) {
            val optionsMap = getSelectedOverlayOptions(context, packageName)
            val options = context.assets.list("$path/options") ?: emptyArray()
            for (option in options) {
                if (optionsMap.containsKey(option)) {
                    val optionsArray = context.assets.list("$path/options/$option") ?: emptyArray()
                    if (optionsArray.isNotEmpty()) {
                        checkResourcePath(context, "$path/options/$option/${optionsMap[option]}", packageName, resourcePaths)
                    }
                }
            }
        }
        if (variants.contains("icons") && useAospIcons(context)) {
            checkResourcePath(context, "$path/icons/aosp", packageName, resourcePaths)
        }
        if (variants.contains("icons") && useStockMultiIcons(context)) {
            checkResourcePath(context, "$path/icons/stock", packageName, resourcePaths)
        }
        if (variants.contains("icons") && usePIcons(context)) {
            checkResourcePath(context, "$path/icons/p", packageName, resourcePaths)
        }
        if (variants.contains("clock") && useLeftClock(context)) {
            checkResourcePath(context, "$path/clock/left", packageName, resourcePaths)
        }
        if (variants.contains("clock") && useCenteredClock(context)) {
            checkResourcePath(context, "$path/clock/centered", packageName, resourcePaths)
        }
        if (variants.contains("style") && usePstyle(context)) {
            checkResourcePath(context, "$path/style/p", packageName, resourcePaths)
        }
    }

    private fun checkOverlay(context: Context, packageName: String) : Boolean {
        val variants = context.assets.list("overlays/$packageName") ?: emptyArray()
        if (variants.contains("versions")) return false

        val resourcePaths = ArrayList<String>()
        val assets = ArrayList<String>()
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

    private fun checkResourcePath(context: Context, path: String, packageName: String, resourcePaths: ArrayList<String>) {
        val variants = context.assets.list(path) ?: return
        if (!variants.contains("props")
                && !variants.contains("versions") && !variants.contains("common")) {
            addResourcePath(resourcePaths, path)
        } else {
            parseOverlayResourcePath(context, path, packageName, resourcePaths)
        }
    }

    private fun addResourcePath(resourcePaths: ArrayList<String>, path: String) {
        resourcePaths.add(path.trimEnd('/'))
    }

    fun parseOverlayAssetPath(am: AssetManager, path: String, assetPaths: ArrayList<String>) {
        val variants = am.list("$path/assets") ?: return
        if (variants.contains("common")) {
            assetPaths.add("$path/assets/common")
        }
    }

    private fun parseOverlayVersions(context: Context, packageName: String, resourcePaths: ArrayList<String>, path: String) {
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