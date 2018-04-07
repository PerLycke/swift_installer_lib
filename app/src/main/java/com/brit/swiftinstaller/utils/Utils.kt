package com.brit.swiftinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
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


object Utils {
    fun getOverlayPackageName(pack: String): String {
        return pack + ".swiftinstaller.overlay"
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
                runCommand("cmd overlay").output!!.contains("packageName")
    }

    fun isOverlayFailed(context: Context, packageName: String): Boolean {
        return false
    }

    fun isSamsungOreo(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile")
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
}