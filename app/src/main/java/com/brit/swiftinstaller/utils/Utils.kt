package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import com.brit.swiftinstaller.ui.applist.AppItem


@Suppress("unused")
object Utils {

    class ListEntry {
        var title: String = ""
        var packageName: String = ""
    }

    fun sortedOverlaysList(context: Context): ArrayList<AppItem> {
        val pm = context.packageManager
        val overlaysList = arrayListOf<AppItem>()
        val getOverlays = context.assets.list("overlays")
        for (pn: String in getOverlays) {
            var info: ApplicationInfo?
            var pInfo: PackageInfo?
            try {
                info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                pInfo = pm.getPackageInfo(pn, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
            if (info != null) {
                val item = AppItem()
                item.packageName = pn
                item.title = info.loadLabel(pm) as String
                item.versionCode = pInfo!!.versionCode
                item.versionName = pInfo.versionName
                overlaysList.add(item)
            }
        }
        overlaysList.sortWith(Comparator { o1, o2 ->
            o1.title.compareTo(o2.title)
        })
        return overlaysList
    }

    fun getOverlayPackageName(pack: String): String {
        return "$pack.swiftinstaller.overlay"
    }

    fun getOverlayPath(packageName: String): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/.swift/" +
                "/overlays/compiled/" + Utils.getOverlayPackageName(packageName) + ".apk"
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
        return Integer.parseInt(ShellUtils.inputStreamToString(context.assets.open(
                "overlays/$targetPackage/version")).trim().replace("\"", ""))
    }

    /*fun checkOverlayStatus() : Boolean {
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
    }*/

    fun isOverlayEnabled(context: Context, packageName: String): Boolean {
        return isSamsungOreo(context) ||
                runCommand("cmd overlay").output!!.contains(packageName)
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

    fun overlayHasVersion(context: Context, packageName: String): Boolean {
        return context.assets.list("overlays/$packageName").contains("versions")
    }

    fun checkVersionCompatible(context: Context, packageName: String): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        if (context.assets.list("overlays/$packageName").contains("versions")) {
            val vers = context.assets.list("overlays/$packageName/versions")
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

    fun createImage(width:Int, height:Int, color:Int):Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    /*fun makeKey(key: File) {
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
    }*/
}