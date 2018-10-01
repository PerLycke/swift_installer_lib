package com.brit.swiftinstaller.library.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap

open class AppExtrasHandler(val context: Context) {

    lateinit var appExtras: ArrayMap<String, (AppCompatActivity) -> Unit>
    lateinit var resourceExtras: ArrayMap<String, (Context, String, String) -> Unit>

    fun initialize() {
        appExtras = populateAppExtras()
        resourceExtras = populateResourceExtras()
    }

    open fun populateAppExtras(): ArrayMap<String, (AppCompatActivity) -> Unit> {
        return ArrayMap()
    }

    open fun populateResourceExtras(): ArrayMap<String, (Context, String, String) -> Unit> {
        return ArrayMap()
    }
}