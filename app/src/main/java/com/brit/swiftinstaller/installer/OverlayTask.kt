package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.os.Environment
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.installer.rom.RomInfo
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
    lateinit var assetDir: File
    lateinit var overlayDir: File
    lateinit var overlayPath: String
    var errorLog = ""
    var uninstall: Boolean = false
    var index = 0

    fun initializeOverlayTask(context: Context, packageName: String, index: Int, uninstall: Boolean) {
        this.context = context
        this.packageName = packageName
        this.packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        this.appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        this.overlayDir = File(Environment.getExternalStorageDirectory(), ".swift/overlays/$packageName")
        this.resDir = File(overlayDir, "res")
        this.assetDir = File(overlayDir, "assets")
        if (resDir.exists())
            resDir.deleteRecursively()
        if (assetDir.exists())
            assetDir.deleteRecursively()
        resDir.mkdirs()
        assetDir.mkdirs()
        this.overlayPath = Utils.getOverlayPath(packageName)
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
            RomInfo.getRomInfo(context).uninstallOverlay(context, packageName)
            mOm.handleState(this, OverlayManager.OVERLAY_UNINSTALLED)
        } else {
            if (!Utils.checkVersionCompatible(context, packageName)) {
                errorLog = "Version Incompatible"
                mOm.handleState(this, OverlayManager.OVERLAY_FAILED)
            } else {
                extractResources()
                compileOverlay()
                deleteFileShell(overlayDir.absolutePath)
            }
            mOm.handleState(this, OverlayManager.OVERLAY_INSTALLED)
        }
    }

    private fun extractResources() {
        val black = useBlackBackground(context)
        if (resDir.exists())
            deleteFileShell(resDir.absolutePath)

        resDir.mkdirs()

        val am = context.assets
        val resourcePaths = ArrayList<String>()
        val assetPaths = ArrayList<String>()
        parseOverlayResourcePath(am, "overlays/$packageName", resourcePaths, black)
        parseOverlayAssetPath(am, "overlays/$packageName", assetPaths, black)
        for (path in resourcePaths) {
            AssetHelper.copyAssetFolder(am, path, resDir.absolutePath, null)
        }
        for (path in assetPaths) {
            AssetHelper.copyAssetFolder(am, path, resDir.absolutePath, null)
        }
        if (packageName == "android") {
            applyAccent(resDir)
        }
        generateManifest(overlayDir.absolutePath, packageName, packageInfo.versionName,
                packageInfo.versionCode, Utils.getThemeVersion(context, packageName))
    }

    private fun checkResourcePath(am: AssetManager, path: String, resourcePaths: ArrayList<String>, black: Boolean) {
        val variants = am.list(path)
        if (!variants.contains("dark")
                && !variants.contains("black") && !variants.contains("common")) {
            addResourcePath(resourcePaths, path)
        } else {
            parseOverlayResourcePath(am, path, resourcePaths, black)
        }
    }

    private fun addResourcePath(resourcePaths: ArrayList<String>, path: String) {
        resourcePaths.add(path)
    }

    private fun parseOverlayAssetPath(am: AssetManager, path: String, assetPaths: ArrayList<String>, black: Boolean) {
        val variants = am.list("$path/assets")
        if (variants.contains("common")) {
            assetPaths.add("$path/assets/common")
        }
        if (variants.contains("black") && black) {
            assetPaths.add("$path/assets/black")
        } else if (variants.contains("dark") && !black) {
            assetPaths.add("$path/assets/dark")
        }
    }

    private fun parseOverlayResourcePath(am: AssetManager, path: String, resourcePaths: ArrayList<String>, black: Boolean) {
        val variants = am.list(path)
        if (variants.contains("common")) {
            checkResourcePath(am, "$path/common", resourcePaths, black)
        }
        for (variant in variants) {
            if (variant == "versions") {
                parseOverlayVersions(am, resourcePaths, "$path/versions", black)
            } else if (!black && variant == "dark") {
                checkResourcePath(am, "$path/dark", resourcePaths, black)
            } else if (black && variant == "black") {
                checkResourcePath(am, "$path/black", resourcePaths, black)
            }
        }
    }

    private fun parseOverlayVersions(am: AssetManager, resourcePaths: ArrayList<String>, path: String, black: Boolean) {
        val vers = am.list(path)
        if (vers.contains("common")) {
            checkResourcePath(am,"$path/common", resourcePaths, black)
        }
        for (ver in vers) {
            if (packageInfo.versionName.startsWith(ver)) {
                checkResourcePath(am, "$path/$ver", resourcePaths, black)
            }
        }
    }

    private fun compileOverlay() {
        val output = ShellUtils.compileOverlay(context, BuildConfig.APPLICATION_ID, resDir.absolutePath,
                overlayDir.absolutePath + "/AndroidManifest.xml",
                overlayPath, null, appInfo)
        if (output.exitCode == 0) {
            RomInfo.getRomInfo(context).installOverlay(context, packageName, overlayPath)
        } else {
            errorLog = output.error
            mOm.handleState(this, OverlayManager.OVERLAY_FAILED)
        }
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
                                 appVersion: String, appVersionCode: Int, themeVersion: Int) {
        val manifest = StringBuilder()
        manifest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        manifest.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        manifest.append("package=\"${Utils.getOverlayPackageName(targetPackage)}\"\n")
        manifest.append("android:versionCode=\"${getAppVersion(context, targetPackage) + 1}\"\n")
        manifest.append("android:versionName=\"${getAppVersion(context, targetPackage) + 1}\">\n")
        manifest.append("<uses-permission android:name=\"com.samsung.android.permission.SAMSUNG_OVERLAY_COMPONENT\" />\n")
        manifest.append("<overlay ")
        manifest.append("android:priority=\"1\" ")
        manifest.append("android:targetPackage=\"$targetPackage\"/>\n")
        manifest.append("<application android:allowBackup=\"false\" android:hasCode=\"false\">\n")
        manifest.append("<meta-data android:name=\"app_version\" android:value=\"v=$appVersion\"/>\n")
        manifest.append("<meta-data android:name=\"app_version_code\" android:value=\"$appVersionCode\"/>\n")
        manifest.append("<meta-data android:name=\"overlay_version\" android:value=\"$themeVersion\"/>\n")
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