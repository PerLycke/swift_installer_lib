package com.brit.swiftinstaller.library.ui.customize

open class Option(val name: String, val value: String) {

    var resTag = ""
    var iconTint = false
    var infoDialogText = ""
    var infoDialogTitle = ""
    var infoText = ""
    val subOptions = OptionsMap()
    var subOptionKey = ""
    var isSliderOption = false

    constructor(name: String, value: String, resTag: String, iconTint: Boolean) : this(name,
            value) {
        this.resTag = resTag
        this.iconTint = iconTint
    }
}