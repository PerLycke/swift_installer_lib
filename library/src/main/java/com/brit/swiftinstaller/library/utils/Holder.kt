package com.brit.swiftinstaller.library.utils

import com.brit.swiftinstaller.library.ui.applist.AppItem

object Holder {
    val installApps: ArrayList<String> = arrayListOf()
    val errorMap: HashMap<String, String> = HashMap()
    val overlaysList = ArrayList<AppItem>()
}