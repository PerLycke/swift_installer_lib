package com.brit.swiftinstaller.library.ui.customize

open class Option(val name: String, val value: String, val resTag: String = "",
                  val iconTint: Boolean = false, val infoDialogText: String = "",
                  val infoDialogTitle: String = "", val infoText: String = "",
                  val subOptions: OptionsMap = OptionsMap(), var subOptionKey: String = "",
                  val isSliderOption: Boolean = false,
                  val requiredApps: Set<String> = setOf())