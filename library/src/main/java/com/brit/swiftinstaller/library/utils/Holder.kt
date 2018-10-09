package com.brit.swiftinstaller.library.utils

object Holder {
    val installApps: SynchronizedArrayList<String> = SynchronizedArrayList()
    val errorMap: HashMap<String, String> = HashMap()
}