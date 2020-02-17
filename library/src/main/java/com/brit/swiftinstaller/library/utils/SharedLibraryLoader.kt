/*
 * Copyright © 2017-2020 WireGuard LLC.
 * Copyright © 2018-2020 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile


object SharedLibraryLoader {
    private val TAG = SharedLibraryLoader.javaClass.name

    fun extractNativeLibrary(
            context: Context,
            libName: String,
            destination: File
    ): Boolean {
        val apks = HashSet<String>()
        if (context.applicationInfo.sourceDir != null)
            apks.add(context.applicationInfo.sourceDir)
        if (context.applicationInfo.splitSourceDirs != null)
            apks.addAll(context.applicationInfo.splitSourceDirs)

        for (apkPath in apks) {
            val zipFile: ZipFile
            try {
                zipFile = ZipFile(File(apkPath), ZipFile.OPEN_READ)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            val mappedLibName = if (libName.contains(".so")) libName else System.mapLibraryName(libName)
            for (abi in Build.SUPPORTED_ABIS) {
                val libZipPath = "lib" + File.separatorChar + abi + File.separatorChar + mappedLibName
                val zipEntry = zipFile.getEntry(libZipPath) ?: continue
                try {
                    Log.d(TAG, "Extracting apk:/$libZipPath to ${destination.absolutePath} and loading")
                    FileOutputStream(destination).use { out ->
                        zipFile.getInputStream(zipEntry).use { inputStream ->
                            inputStream.copyTo(out)
                        }
                    }
                    zipFile.close()
                    return true
                } catch (e: Exception) {
                    Log.d(TAG, "Failed to load library apk:/$libZipPath", e)
                    throw e
                }
            }
            zipFile.close()
            return false
        }
        return false
    }
}
