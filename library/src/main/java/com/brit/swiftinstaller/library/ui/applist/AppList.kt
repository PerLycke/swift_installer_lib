package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.brit.swiftinstaller.library.utils.SynchronizeArrayList
import com.brit.swiftinstaller.library.utils.getAppsToUpdate
import com.brit.swiftinstaller.library.utils.getHiddenApps
import com.brit.swiftinstaller.library.utils.getVersionCode
import com.brit.swiftinstaller.library.utils.swift

object AppList {

    private const val INACTIVE = 0
    private const val ACTIVE = 1
    private const val UPDATE = 2

    val appUpdates = SynchronizeArrayList<AppItem>()
    val activeApps = SynchronizeArrayList<AppItem>()
    val inactiveApps = SynchronizeArrayList<AppItem>()

    private val subscribers = ArrayList<(Int) -> Unit>()

    fun initLists(context: Context) {
        val disabledOverlays = context.swift.romInfo.getDisabledOverlays()
        val hiddenOverlays = getHiddenApps(context)
        val pm = context.packageManager
        val overlays = context.assets.list("overlays") ?: emptyArray()
        val extras = context.swift.extrasHandler.appExtras.keys
        for (pn: String in overlays.subtract(extras).plus(extras)) {
            if (!extras.contains(pn) && disabledOverlays.contains(pn)) continue
            if (hiddenOverlays.contains(pn)) continue
            var info: ApplicationInfo
            try {
                info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
            if (!info.enabled) {
                continue
            }
            addApp(context, pn)
        }
        appUpdates.sortWith(Comparator { o1, o2 ->
            o1.title.compareTo(o2.title)
        })
        activeApps.sortWith(Comparator { o1, o2 ->
            o1.title.compareTo(o2.title)
        })
        inactiveApps.sortWith(Comparator { o1, o2 ->
            o1.title.compareTo(o2.title)
        })
    }

    fun addSubscriber(add: (Int) -> Unit) {
        subscribers.add(add)
    }

    private fun updateSubscribers(list: Int) {
        subscribers.forEach {
            it.invoke(list)
        }
    }

    fun addApp(context: Context, packageName: String) {
        removeApp(context, packageName)
        val updates = getAppsToUpdate(context)
        val item = AppItem()
        val pInfo = context.packageManager.getPackageInfo(packageName, 0)
        item.packageName = packageName
        item.title = pInfo.applicationInfo.loadLabel(context.packageManager) as String
        item.versionCode = pInfo.getVersionCode()
        item.versionName = pInfo.versionName
        item.icon = pInfo.applicationInfo.loadIcon(context.packageManager)
        if (context.swift.romInfo.isOverlayInstalled(packageName)) {
            if (updates.contains(packageName)) {
                appUpdates.add(item)
                updateSubscribers(UPDATE)
            } else {
                activeApps.add(item)
                updateSubscribers(ACTIVE)
            }
        } else {
            inactiveApps.add(item)
            updateSubscribers(INACTIVE)
        }
    }

    fun removeApp(@Suppress("UNUSED_PARAMETER") context: Context, packageName: String) {
        var item: AppItem? = null
        appUpdates.forEach {
            if (it.packageName == packageName) {
                item = it
                updateSubscribers(UPDATE)
                return@forEach
            }
        }
        item?.let {
            appUpdates.remove(it)
            item = null
        }
        activeApps.forEach {
            if (it.packageName == packageName) {
                item = it
                updateSubscribers(ACTIVE)
                return@forEach
            }
        }
        item?.let {
            activeApps.remove(it)
            item = null
        }
        inactiveApps.forEach {
            if (it.packageName == packageName) {
                item = it
                updateSubscribers(INACTIVE)
                return@forEach
            }
        }
        item?.let { inactiveApps.remove(it) }
    }
}