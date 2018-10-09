package com.brit.swiftinstaller.library.ui.customize

import com.brit.swiftinstaller.library.utils.SynchronizedArrayList

class CustomizeCategory(val name: String, val key: String, val default: String,
                        val options: OptionsMap, val requiredApps: SynchronizedArrayList<String>)