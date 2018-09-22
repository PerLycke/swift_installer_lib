package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.getAppsToUpdate
import com.brit.swiftinstaller.library.utils.getHiddenApps
import com.brit.swiftinstaller.library.utils.getVersionCode
import com.brit.swiftinstaller.library.utils.swift

object AppList {

    private const val INACTIVE = 0
    private const val ACTIVE = 1
    private const val UPDATE = 2

    val appUpdates = AppItemArrayList()
    val activeApps = AppItemArrayList()
    val inactiveApps = AppItemArrayList()

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
        for (i in list) {
            if (item.title.compareTo(i.title, false) < 0) {
                return list.indexOf(i)
            }
        }
        return list.size
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
            if (appUpdates.contains(packageName)) {
                appUpdates.remove(packageName)
                updateSubscribers(UPDATE)
            }
            if (activeApps.contains(packageName)) {
                activeApps.remove(packageName)
                updateSubscribers(ACTIVE)
            }
            if (inactiveApps.contains(packageName)) {
                inactiveApps.remove(packageName)
                updateSubscribers(INACTIVE)
            }
        }
    }
}

class AppItemArrayList: SynchronizedArrayList<AppItem>() {
    fun contains(packageName: String): Boolean {
        forEach {
            if (packageName == it.packageName) {
                return true
            }
        }
        return false
    }
    fun remove(packageName: String) {
        removeIf { it.packageName == packageName }
    }
}