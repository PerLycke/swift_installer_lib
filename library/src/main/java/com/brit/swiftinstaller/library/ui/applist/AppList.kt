package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.brit.swiftinstaller.library.utils.*

object AppList {

    private const val INACTIVE = 0
    private const val ACTIVE = 1
    private const val UPDATE = 2

    val appUpdates = SynchronizedArrayList<AppItem>()
    val activeApps = SynchronizedArrayList<AppItem>()
    val inactiveApps = SynchronizedArrayList<AppItem>()

    private val subscribers = SynchronizedArrayList<(Int) -> Unit>()

    @Synchronized
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
//        appUpdates.sortWith(Comparator { o1, o2 ->
//            o1.title.compareTo(o2.title)
//        })
//        activeApps.sortWith(Comparator { o1, o2 ->
//            o1.title.compareTo(o2.title)
//        })
//        inactiveApps.sortWith(Comparator { o1, o2 ->
//            o1.title.compareTo(o2.title)
//        })
    }

    @Synchronized
    fun addSubscriber(add: (Int) -> Unit) {
        synchronized(this) {
            subscribers.add(add)
        }
    }

    @Synchronized
    private fun updateSubscribers(list: Int) {
        synchronized(this) {
            subscribers.forEach {
                it.invoke(list)
            }
        }
    }

    private fun getPosition(item: AppItem, list: SynchronizedArrayList<AppItem>) : Int {
        var position = 0
        for (i in 0 until list.size) {
            val name = list[i].title
            if (item.title.compareTo(name, false) < 0) {
                break
            }
            position++
        }
        return position
    }

    @Synchronized
    fun addApp(context: Context, packageName: String) {
        synchronized(this) {
            removeApp(context, packageName)
            val updates = getAppsToUpdate(context)
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            val item = AppItem(packageName,
                    pInfo.applicationInfo.loadLabel(context.packageManager) as String,
                    pInfo.getVersionCode(),
                    pInfo.versionName,
                    pInfo.applicationInfo.loadIcon(context.packageManager))
            if (context.swift.romInfo.isOverlayInstalled(packageName)) {
                if (updates.contains(packageName)) {
                    appUpdates.add(getPosition(item, appUpdates), item)
                    updateSubscribers(UPDATE)
                } else {
                    activeApps.add(getPosition(item, activeApps), item)
                    updateSubscribers(ACTIVE)
                }
            } else {
                inactiveApps.add(getPosition(item, inactiveApps), item)
                updateSubscribers(INACTIVE)
            }
        }
    }

    @Synchronized
    fun removeApp(@Suppress("UNUSED_PARAMETER") context: Context, packageName: String) {
        synchronized(this) {
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
}