package com.brit.swiftinstaller.library.ui.changelog

import android.content.Context
import com.michaelflisar.changelog.tags.IChangelogTag

class SwiftChangelogTag(val tag: String, private val prefix: String): IChangelogTag {
    override fun getXMLTagName(): String {
        return tag
    }

    override fun formatChangelogRow(context: Context, changeText: String): String {
        return if (prefix.isEmpty()) {
            changeText
        } else {
            "<font color=\"#fafafa\"><b>$prefix: </b></font>$changeText"
        }
    }

}