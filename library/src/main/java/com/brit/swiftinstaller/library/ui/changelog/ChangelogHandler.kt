package com.brit.swiftinstaller.library.ui.changelog

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.utils.swift
import com.michaelflisar.changelog.ChangelogBuilder
import com.michaelflisar.changelog.ChangelogSetup
import com.michaelflisar.changelog.interfaces.IChangelogFilter
import com.michaelflisar.changelog.interfaces.IRecyclerViewItem
import com.michaelflisar.changelog.items.ItemRow
import com.michaelflisar.changelog.internal.ChangelogRecyclerViewAdapter
import kotlinx.android.parcel.Parcelize

object ChangelogHandler {

    fun showChangelog(activity: AppCompatActivity, managedShow: Boolean) {
        val setup = ChangelogSetup.get()
        setup.clearTags()
        setup.registerTag(SwiftChangelogTag("oreo", "AOSP Oreo"))
        setup.registerTag(SwiftChangelogTag("oos-oreo", "OOS Oreo"))
        setup.registerTag(SwiftChangelogTag("oos-p", "OOS Pie"))
        setup.registerTag(SwiftChangelogTag("oos-q", "OOS 10"))
        setup.registerTag(SwiftChangelogTag("p", "AOSP Pie"))
        setup.registerTag(SwiftChangelogTag("aosp-q", "AOSP 10"))
        setup.registerTag(SwiftChangelogTag("samsung", "Samsung"))
        setup.registerTag(SwiftChangelogTag("samsung-p", "Samsung Pie"))
        setup.registerTag(SwiftChangelogTag("miui-p", "Miui Pie"))
        setup.registerTag(SwiftChangelogTag("miui-q", "Miui Android 10"))
        setup.registerTag(SwiftChangelogTag("installer", "Installer"))
        setup.registerTag(SwiftChangelogTag("common", ""))

        val builder = ChangelogBuilder()
                .withUseBulletList(true)
                .withFilter(SwiftChangelogFilter(activity.swift.romHandler.getChangelogTag()))

        if (managedShow) {
            builder.withMinVersionToShow(BuildConfig.VERSION_CODE)
        }

        if ((managedShow && shouldShowDialog(activity)) || !managedShow) {
            ChangelogDialog.show(activity, builder)
        }
    }

    private fun shouldShowDialog(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentVer = BuildConfig.VERSION_CODE
        val lastShownVer = pref.getInt("changelog_version_code", -1)
        if (lastShownVer == -1) {
            pref.edit().putInt("changelog_version_code", currentVer).apply()
            return false
        }
        if (currentVer > lastShownVer) {
            pref.edit().putInt("changelog_version_code", currentVer).apply()
            return true
        }
        return false
    }

    @Parcelize
    class SwiftChangelogFilter(val tag: String) : IChangelogFilter {

        override fun checkFilter(item: IRecyclerViewItem): Boolean {
            if (item.recyclerViewType == ChangelogRecyclerViewAdapter.Type.Row) {
                val tag = (item as ItemRow).tag.xmlTagName
                if (tag != this.tag && tag != "installer" && tag != "common") {
                    return false
                }
            }
            return true
        }

        override fun describeContents(): Int {
            return 0
        }
    }
}