package com.brit.swiftinstaller.library.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap

open class AppExtrasHandler(val context: Context) {

    lateinit var appExtras: ArrayMap<String, (AppCompatActivity) -> Unit>

    fun initialize() {
        appExtras = populateAppExtras()
    }

    open fun populateAppExtras(): ArrayMap<String, (AppCompatActivity) -> Unit> {
        return ArrayMap()
    }
}