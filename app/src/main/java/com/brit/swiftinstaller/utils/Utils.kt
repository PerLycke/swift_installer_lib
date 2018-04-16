package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.brit.swiftinstaller.R
import org.bouncycastle.x509.X509V3CertificateGenerator
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object Utils {

    fun getOverlayPackageName(pack: String): String {
        return "$pack.swiftinstaller.overlay"
    }

    fun mapToBundle(map: HashMap<String, String>): Bundle {
        val bundle = Bundle()
        for (key in map.keys) {
            bundle.putString(key, map[key])
        }
        return bundle
    }

    fun bundleToMap(bundle: Bundle): HashMap<String, String> {
        val map = HashMap<String, String>()
        for (key in bundle.keySet()) {
            map[key] = bundle.getString(key)
        }
        return map
    }

    fun isOverlayInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getThemeVersion(context: Context, targetPackage: String): Int {
        val ver = ShellUtils.inputStreamToString(context.assets.open(
                "overlays/$targetPackage/version"))
        Log.d("TEST", "$targetPackage version - $ver")
        return 1
    }

    fun checkOverlayStatus() : Boolean {
        try {
            val pi = Class.forName("android.content.pm.PackageInfo")
            for (field : Field in pi.declaredFields) {
                if (field.name == "FLAG_OVERLAY_STATIC" || field.name == "FLAG_OVERLAY_TRUSTED") {
                    return true
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isOverlayEnabled(context: Context, packageName: String): Boolean {
        return isSamsungOreo(context) ||
                runCommand("cmd overlay").output!!.contains(packageName)
    }

    fun containsOverlay(context: Context, packageName: String): Boolean {
        val apps = context.assets.list("overlays")
        return apps.contains(packageName)
    }

    fun isSamsungOreo(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile")
    }

    fun checkAppVersion(context: Context, packageName: String): Boolean {
        val appVersionCode = context.packageManager.getPackageInfo(packageName, 0).versionCode
        val curVersionCode = context.packageManager.getApplicationInfo(
                Utils.getOverlayPackageName(packageName),
                PackageManager.GET_META_DATA).metaData.getInt("app_version_code")
        return appVersionCode > curVersionCode
    }

    fun checkVersionCompatible(context: Context, packageName: String): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        if (context.assets.list("overlays/$packageName").contains("versions")) {
            val vers = context.assets.list("overlays/$packageName/versions")
            Log.d("TEST", "$packageName - ${packageInfo.versionName}")
            for (ver in vers) {
                Log.d("TEST", "Available ver - $ver")
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
        for (version in context.assets.list("overlays/$packageName/versions")) {
            if (version != "common") {
                versions.append("v$version, ")
            }
        }
        return versions.substring(0, versions.length - 2)
    }

    fun checkOverlayVersion(context: Context, packageName: String): Boolean {
        val overlayVersion = Integer.parseInt(ShellUtils.inputStreamToString(context.assets.open(
                "overlays/$packageName/version")).trim().replace("\"", ""))
        val currentVersion = context.packageManager.getApplicationInfo(
                Utils.getOverlayPackageName(packageName),
                PackageManager.GET_META_DATA).metaData.getInt("overlay_version")
        return overlayVersion > currentVersion
    }

    fun getInstalledOverlays(context: Context): ArrayList<String> {
        val apps = ArrayList<String>()
        for (app in context.assets.list("overlays")) {
            if (isOverlayInstalled(context, Utils.getOverlayPackageName(app))) {
                apps.add(app)
            }
        }
        return apps
    }

    fun getDialogTheme(context: Context): Int {
        if (useBlackBackground(context)) {
            return R.style.AppTheme_AlertDialog_Black
        } else {
            return R.style.AppTheme_AlertDialog
        }
    }

    fun makeKey(key: File) {
        val keyPass = "overlay".toCharArray()

        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(1024, SecureRandom.getInstance("SHA1PRNG"))
        val keyPair = keyGen.genKeyPair()
        val privKey = keyPair.private

        val cert = generateX509Certificate(keyPair)
        val chain = Array(1, { cert!! })

        val store = KeyStore.getInstance(KeyStore.getDefaultType())
        store.load(null, null)
        store.setKeyEntry("key", privKey, keyPass, chain)
        store.setCertificateEntry("cert", cert)
        store.store(FileOutputStream(key), keyPass)
        key.copyTo(File(Environment.getExternalStorageDirectory(), "signing-key"))
    }

    private fun generateX509Certificate(keyPair: KeyPair): X509Certificate? {
        try {
            val calendar = Calendar.getInstance()
            calendar.time = Date(System.currentTimeMillis())
            val begDate = calendar.time
            calendar.add(Calendar.YEAR, 25)
            val endDate = calendar.time

            val gen = X509V3CertificateGenerator()
            val dnn = X500Principal("CN=swift-installer")
            gen.setSignatureAlgorithm("SHA256WithRSAEncryption")
            gen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
            gen.setSubjectDN(dnn)
            gen.setIssuerDN(dnn)
            gen.setNotBefore(begDate)
            gen.setNotAfter(endDate)
            gen.setPublicKey(keyPair.public)
            return gen.generate(keyPair.private)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @param color
     * @throws NumberFormatException
     * @author Unknown
     */
    @Throws(NumberFormatException::class)
    fun convertToColorInt(color: String): Int {
        var argb = color

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "")
        }

        var alpha = -1
        var red = -1
        var green = -1
        var blue = -1

        if (argb.length == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16)
            red = Integer.parseInt(argb.substring(2, 4), 16)
            green = Integer.parseInt(argb.substring(4, 6), 16)
            blue = Integer.parseInt(argb.substring(6, 8), 16)
        } else if (argb.length == 6) {
            alpha = 255
            red = Integer.parseInt(argb.substring(0, 2), 16)
            green = Integer.parseInt(argb.substring(2, 4), 16)
            blue = Integer.parseInt(argb.substring(4, 6), 16)
        }

        return Color.argb(alpha, red, green, blue)
    }
}