package com.brit.swiftinstaller.ui.applist

import android.graphics.drawable.Drawable

class AppItem {
    var packageName: String = ""
    var title: String = ""
    var required: String = ""
    var versionCode: Int = 0
    var versionName: String = ""
    var icon: Drawable? = null
}