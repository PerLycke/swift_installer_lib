@file:Suppress("unused")

package com.brit.swiftinstaller.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.system.ErrnoException
import android.system.Os
import android.text.TextUtils
import android.util.Log
import com.android.apksig.ApkSigner
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

//import kellinwood.security.zipsigner.ZipSigner;

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ShellUtils {

    private val TAG = ShellUtils::class.java.simpleName

    val isRootAvailable: Boolean
        get() {
            var output: CommandOutput? = runCommand("id")
            return if (output != null && TextUtils.isEmpty(output.error) && output.exitCode == 0) {
                output.output != null && output.output!!.contains("uid=0")
            } else {
                output = runCommand("echo _TEST_")
                output.output!!.contains("_TEST_")
            }
        }

    fun listFiles(path: String): Array<String> {
        val output = runCommand("ls " + path)
        return output.output!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun inputStreamToString(`is`: InputStream?): String {
        val s = java.util.Scanner(`is`!!).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun compileOverlay(context: Context, themePackage: String, res: String?, manifest: String,
                       overlayPath: String, assetPath: String?, targetInfo: ApplicationInfo?) {
        val overlay = File(overlayPath)
        val unsigned = File(overlay.parent, "unsigned_" + overlay.name)
        val overlayFolder = File(context.cacheDir.toString() + "/" + themePackage + "/overlays")
        if (!overlayFolder.exists()) {
            if (!overlayFolder.mkdirs()) {
                Log.e(TAG, "Unable to create " + overlayFolder.absolutePath)
            }
        }

        if (fileExists(unsigned.absolutePath) && !deleteFileShell(unsigned.absolutePath)) {
            Log.e(TAG, "Unable to delete " + unsigned.absolutePath)
        }
        if (fileExists(overlay.absolutePath) && !deleteFileShell(overlay.absolutePath)) {
            Log.e(TAG, "Unable to delete " + overlay.absolutePath)
        }

        val cmd = StringBuilder()
        cmd.append(getAapt(context))
        cmd.append(" p")
        cmd.append(" -M ").append(manifest)
        if (res != null) {
            cmd.append(" -S ").append(res)
        }
        if (assetPath != null) {
            cmd.append(" -A ").append(assetPath)
        }
        cmd.append(" -I ").append("/system/framework/framework-res.apk")
        if (targetInfo != null && targetInfo.packageName != "android") {
            cmd.append(" -I ").append(targetInfo.sourceDir)
        }
        cmd.append(" -F ").append(unsigned.absolutePath)
        //ShellUtils.runCommand(cmd.toString());
        try {
            val aapt = Runtime.getRuntime().exec(cmd.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            val exitCode = aapt.waitFor()
            val error = inputStreamToString(aapt.errorStream)
            val output = inputStreamToString(aapt.inputStream)
            Log.d("TEST", "aapt exitCode - " + exitCode)
            Log.d("TEST", "aapt output - " + output)
            Log.d("TEST", "aapt error - " + error)
            aapt.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (unsigned.exists()) {
            runCommand("chmod 777 " + unsigned, false)
            val key = File(context.dataDir, "/signing-key")
            val keyPass = "overlay".toCharArray()
            if (!key.exists()) {
                Utils.makeKey(key)
            }

            val ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(FileInputStream(key), keyPass)
            val pk = ks.getKey("key", keyPass) as PrivateKey
            val certs = ArrayList<X509Certificate>()
            certs.add(ks.getCertificateChain("key")[0] as X509Certificate)
            val signConfig = ApkSigner.SignerConfig.Builder("overlay", pk, certs).build()
            val signConfigs = ArrayList<ApkSigner.SignerConfig>()
            signConfigs.add(signConfig)
            val signer = ApkSigner.Builder(signConfigs)
            signer.setV1SigningEnabled(false)
                    .setV2SigningEnabled(true)
                    .setInputApk(unsigned)
                    .setOutputApk(overlay)
                    .setMinSdkVersion(Build.VERSION.SDK_INT)
                    .build()
                    .sign()
        }
    }

    private fun getAapt(context: Context): String? {
        val aapt = File(context.cacheDir, "aapt")
        if (aapt.exists()) return aapt.absolutePath
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = context.assets.open("aapt")
            out = FileOutputStream(aapt)
            val bytes = ByteArray(1024)
            var len: Int = `in`.read(bytes)
            while (len != -1) {
                out.write(bytes, 0, len)
                len = `in`.read(bytes)
            }
            Os.chmod(aapt.absolutePath, 755)
            return aapt.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: ErrnoException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                if (`in` != null) {
                    `in`.close()
                }
                if (out != null) {
                    out.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}

fun runCommand(cmd: String): CommandOutput {
    return runCommand(cmd, false)
}

fun runCommand(cmd: String, root: Boolean = true): CommandOutput {
    var os: DataOutputStream? = null
    var process: Process? = null
    try {

        val pb = ProcessBuilder(if (root) "su" else "sh")
        process = pb.start()
        os = DataOutputStream(process!!.outputStream)
        os.writeBytes(cmd + "\n")
        os.flush()
        os.writeBytes("exit\n")
        os.flush()

        val input = process.inputStream
        val error = process.errorStream

        val `in` = ShellUtils.inputStreamToString(input)
        val err = ShellUtils.inputStreamToString(error)

        input?.close()
        error?.close()

        process.waitFor()

        val output = CommandOutput(`in`, err, process.exitValue())

        Log.d("TEST", "cmd - " + cmd)
        Log.d("TEST", "output - " + `in`)
        Log.d("TEST", "error - " + err)
        return output
    } catch (e: IOException) {
        e.printStackTrace()
        return CommandOutput("", "", 1)
    } catch (e: InterruptedException) {
        e.printStackTrace()
        return CommandOutput("", "", 1)
    } finally {
        try {
            if (os != null) {
                os.close()
            }
            if (process != null) {
                process.destroy()
            }
        } catch (ignored: IOException) {
        }

    }
}

fun fileExists(path: String): Boolean {
    val output = runCommand("ls " + path)
    return output.exitCode == 0
}

fun remountRW(path: String): Boolean {
    return remount(path, "rw")
}

fun remountRO(path: String): Boolean {
    return remount(path, "ro")
}

private fun remount(path: String, type: String): Boolean {
    val readlink = runCommand("readlink $(which mount)")
    val mount: CommandOutput
    mount = if (readlink.output!!.contains("toolbox")) {
        runCommand("mount -o remount,$type $path")
    } else {
        runCommand("mount -o $type,remount $path")
    }
    return mount.exitCode == 0
}

fun deleteFileShell(path: String): Boolean {
    val output = runCommand("rm -rf " + path, false)
    return output.exitCode == 0
}

@SuppressLint("PrivateApi")
fun getProperty(name: String): String? {
    try {
        @SuppressLint("PrivateApi") val clazz = Class.forName("android.os.SystemProperties")
        val m = clazz.getDeclaredMethod("get", String::class.java)
        return m.invoke(null, name) as String
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        return null
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
        return null
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
        return null
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
        return null
    }

}
