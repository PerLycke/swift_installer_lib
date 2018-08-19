package com.brit.swiftinstaller.utils

import android.content.res.AssetManager
import java.io.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream


@Suppress("MemberVisibilityCanBePrivate")
class AssetHelper {
    companion object {
        fun copyAssetFolder(am: AssetManager?, assetPath: String, path: String,
                            cipher: Cipher?): Boolean {
            try {
                val files = am!!.list(assetPath) ?: emptyArray()
                val f = File(path)
                if (!f.exists() && !f.mkdirs()) {
                    throw RuntimeException("cannot create directory: $path")
                }
                var res = true
                for (file in files) {
                    val assetList = am.list("$assetPath/$file") ?: emptyArray()
                    res = if (assetList.isEmpty()) {
                        res and copyAsset(am, "$assetPath/$file", "$path/$file", cipher)
                    } else {
                        res and copyAssetFolder(am, "$assetPath/$file", "$path/$file", cipher)
                    }
                }
                return res
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

        }

        fun copyAsset(am: AssetManager?, assetPath: String, realPath: String,
                      cipher: Cipher?): Boolean {
            var path = realPath
            var `in`: InputStream? = null
            var out: OutputStream? = null
            val parent = File(path).parentFile
            if (!parent.exists() && !parent.mkdirs()) {
                throw RuntimeException("cannot create directory: " + parent.absolutePath)
            }

            if (path.endsWith(".enc")) {
                path = path.substring(0, path.lastIndexOf("."))
            }

            try {
                `in` = if (cipher != null && assetPath.endsWith(".enc")) {
                    CipherInputStream(am!!.open(assetPath), cipher)
                } else {
                    am!!.open(assetPath)
                }
                out = FileOutputStream(File(path))
                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                var len: Int = `in`!!.read(bytes)
                while (len != -1) {
                    out.write(bytes, 0, len)
                    len = `in`.read(bytes)
                }
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            } finally {
                try {
                    `in`?.close()
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}