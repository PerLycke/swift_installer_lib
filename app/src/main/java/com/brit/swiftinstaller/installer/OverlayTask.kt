package com.brit.swiftinstaller.installer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Environment
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.AssetHelper
import com.brit.swiftinstaller.utils.ShellUtils
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.rom.RomInfo
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

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
        if (!resDir.exists())
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
            mOm.handleState(this, OverlayManager.OVERLAY_INSTALLED)
        }
    }

    private fun extractResources() {

        if (!resDir.exists())
            resDir.mkdirs()

        val am = context.assets
        val assetPaths = ArrayList<String>()
        assetPaths.add("overlays/$packageName/res")
        val ri = RomInfo.getRomInfo(context)
        try {
            val variants = am.list("overlays/$packageName")
            ri.variants
                    .filter { variants != null && Arrays.asList(*variants).contains(it) }
                    .mapTo(assetPaths) { "overlays/$packageName/$it" }
        } catch (ignored: Exception) {
        }

        for (path in assetPaths) {
            AssetHelper.copyAssetFolder(am, path, resDir.absolutePath, null)
        }
        if (packageName == "android") {
            applyAccent(resDir)
        }
        generateManifest(overlayDir.absolutePath,
                BuildConfig.APPLICATION_ID, packageName, packageInfo.versionName)
    }

    private fun compileOverlay() {
        ShellUtils.compileOverlay(context, BuildConfig.APPLICATION_ID, resDir.absolutePath,
                overlayDir.absolutePath + "/AndroidManifest.xml",
                overlayPath, null, appInfo)
        RomInfo.getRomInfo(context).installOverlay(context, packageName, overlayPath)
    }

    private fun applyAccent(resDir: File) {
        val accent = getAccentColor(context)
        //val hightlightColor =
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
            writer = BufferedWriter(FileWriter(resDir.absolutePath + "/values/type1a.xml"))
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

    private fun generateManifest(path: String, packageName: String?,
                                 targetPackage: String, themeVersion: String) {
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
        manifest.append("<meta-data android:name=\"theme_version\" android:value=\"v="
                + themeVersion + "\"/>\n")
        manifest.append("<meta-data android:name=\"theme_package\" android:value=\""
                + packageName + "\"/>\n")
        manifest.append("<meta-data android:name=\"target_package\" android:value=\""
                + targetPackage + "\"/>\n")
        manifest.append("</application>\n")
        manifest.append("</manifest>")

        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(FileWriter(path + "/AndroidManifest.xml"))
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