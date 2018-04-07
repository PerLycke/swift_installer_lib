package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.rom.RomInfo
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

@Suppress("MemberVisibilityCanBePrivate")
class OverlayTask(val mOm: OverlayManager) : Runnable {

    lateinit var context: Context

    lateinit var packageName: String
    lateinit var packageInfo: PackageInfo
    lateinit var appInfo: ApplicationInfo
    lateinit var resDir: File
    lateinit var overlayDir: File
    lateinit var overlayPath: String
    var uninstall: Boolean = false
    var index = 0

    fun initializeOverlayTask(context: Context, packageName: String, index: Int, uninstall: Boolean) {
        this.context = context
        this.packageName = packageName
        this.packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        this.appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        this.overlayDir = File(Environment.getExternalStorageDirectory(), ".swift/overlays/$packageName")
        this.resDir = File(overlayDir, "res")
        if (resDir.exists())
            resDir.deleteRecursively()
        resDir.mkdirs()
        this.overlayPath = Environment.getExternalStorageDirectory().absolutePath + "/.swift/" +
                "/overlays/compiled/" + Utils.getOverlayPackageName(packageName) + ".apk"
        if (!File(overlayPath).parentFile.exists())
            File(overlayPath).parentFile.mkdirs()
        this.index = index
        this.uninstall = uninstall
    }

    fun getRunnable(): Runnable {
        return this
    }

    override fun run() {
        if (uninstall) {
            RomInfo.getRomInfo(context).uninstallOverlay(context, packageName)
            mOm.handleState(this, OverlayManager.OVERLAY_UNINSTALLED)
        } else {
            extractResources()
            compileOverlay()
            deleteFileShell(overlayDir.absolutePath)
            mOm.handleState(this, OverlayManager.OVERLAY_INSTALLED)
        }
    }

    private fun extractResources() {
        val black = useBlackBackground(context)
        if (resDir.exists())
            deleteFileShell(resDir.absolutePath)

        resDir.mkdirs()

        val am = context.assets
        val assetPaths = ArrayList<String>()
        try {
            parseOverlayAssetPath(am, "overlays/$packageName", assetPaths, black)
        } catch (ignored: Exception) {
        }

        for (path in assetPaths) {
            Log.d("TEST", "asset path - $path")
            AssetHelper.copyAssetFolder(am, path, resDir.absolutePath, null)
        }
        if (packageName == "android") {
            applyAccent(resDir)
        }
        generateManifest(overlayDir.absolutePath, packageName, packageInfo.versionName,
                Utils.getThemeVersion(context, packageName))
    }

    private fun checkAssetPath(am: AssetManager, path: String): Boolean {
        val variants = am.list(path)
        return !variants.contains("dark")
                && !variants.contains("black") && !variants.contains("common")
    }

    private fun addAssetPath(assetPaths: ArrayList<String>, asset: String) {
        assetPaths.add(asset)
    }

    private fun parseOverlayAssetPath(am: AssetManager, path: String, assetPaths: ArrayList<String>, black: Boolean) {
        val variants = am.list(path)
        if (variants.contains("common")) {
            if (checkAssetPath(am, "$path/common")) {
                addAssetPath(assetPaths, "$path/common")
            } else {
                parseOverlayAssetPath(am, "$path/common", assetPaths, black)
            }
        }
        for (variant in variants) {
            if (packageInfo.versionName.startsWith(variant)) {
                parseOverlayAssetPath(am, "$path/$variant", assetPaths, black)
            } else if (!black && variant == "dark") {
                if (checkAssetPath(am, "$path/dark")) {
                    addAssetPath(assetPaths, "$path/dark")
                } else {
                    parseOverlayAssetPath(am, "$path/dark", assetPaths, black)
                }
            } else if (black && variant == "black") {
                if (checkAssetPath(am, "$path/black")) {
                    addAssetPath(assetPaths, "$path/black")
                } else {
                    parseOverlayAssetPath(am, "$path/black", assetPaths, black)
                }
            } else if (checkAssetPath(am, path)) {
                addAssetPath(assetPaths, path)
            }
        }
    }

    private fun compileOverlay() {
        ShellUtils.compileOverlay(context, BuildConfig.APPLICATION_ID, resDir.absolutePath,
                overlayDir.absolutePath + "/AndroidManifest.xml",
                overlayPath, null, appInfo)
        RomInfo.getRomInfo(context).installOverlay(context, packageName, overlayPath)
    }

    private fun applyAccent(resDir: File) {
        val accent = getAccentColor(context)
        val file = StringBuilder()
        file.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        file.append("<resources>\n")
        file.append("<color name=\"material_blue_grey_900\">#" + String.format("%06x", accent).substring(2) + "</color>\n")
        //file.append("<color name=\"highlighted_text_dark\">")
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
                if (writer != null) {
                    writer.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun generateManifest(path: String, targetPackage: String,
                                 appVersion: String, themeVersion: Int) {
        val manifest = StringBuilder()
        manifest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        manifest.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        manifest.append("package=\"" + Utils.getOverlayPackageName(targetPackage) + "\">\n")
        manifest.append("<uses-permission android:name=\"com.samsung.android.permission.SAMSUNG_OVERLAY_COMPONENT\" />\n")
        manifest.append("<overlay ")
        manifest.append("android:priority=\"1\" ")
        if (targetPackage == "android") {
            manifest.append("android:targetPackage=\"android\"/>\n")
        } else {
            manifest.append("android:targetPackage=\"$targetPackage\"/>\n")
        }
        manifest.append("<application android:allowBackup=\"false\" android:hasCode=\"false\">\n")
        manifest.append("<meta-data android:name=\"app_version\" android:value=\"v=$appVersion\"/>\n")
        manifest.append("<meta-data android:name=\"theme_version\" android:value=\"$themeVersion\"/>\n")
        manifest.append("<meta-data android:name=\"target_package\" android:value=\"$targetPackage\"/>\n")
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
                if (writer != null) {
                    writer.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}