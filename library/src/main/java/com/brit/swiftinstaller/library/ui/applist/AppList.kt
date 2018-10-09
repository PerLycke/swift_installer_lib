package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.brit.swiftinstaller.library.utils.*

object AppList {

    const val INACTIVE = 0
    const val ACTIVE = 1
    const val UPDATE = 2

    val appUpdates = AppItemArrayList()
    val activeApps = AppItemArrayList()
    val inactiveApps = AppItemArrayList()

    private val subscribers = SynchronizedArrayList<(Int) -> Unit>()
    private fun updates(context: Context): Set<String> = getAppsToUpdate(context)

    @Synchronized
    fun updateList(context: Context) {
        val disabledOverlays = context.swift.romHandler.getDisabledOverlays()
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
            updateApp(context, pn)
        }
    }

    @Synchronized
    fun addSubscriber(add: (Int) -> Unit) {
        synchronized(this) {
            subscribers.add(add)
        }
    }

    @Synchronized
    fun removeSubscriber(subscription: (Int) -> Unit) {
        synchronized(this) {
            subscribers.remove(subscription)
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

    private fun contains(packageName: String): Int {
        if (appUpdates.contains(packageName)) return UPDATE
        if (inactiveApps.contains(packageName)) return INACTIVE
        if (activeApps.contains(packageName)) return ACTIVE
        return -1
    }

    private fun getPackageIndex(context: Context, packageName: String): Int {
        return if (context.swift.romHandler.isOverlayInstalled(packageName)) {
            if (updates(context).contains(packageName)) {
                UPDATE
            } else {
                ACTIVE
            }
        } else {
            INACTIVE
        }
    }

    private fun putApp(item: AppItem, index: Int) {
        when(index) {
            UPDATE -> {
                appUpdates.add(getPosition(item, appUpdates), item)
                activeApps.add(getPosition(item, activeApps), item)
                updateSubscribers(index)
                updateSubscribers(ACTIVE)
            }
            ACTIVE -> {
                activeApps.add(getPosition(item, activeApps), item)
                updateSubscribers(index)
            }
            INACTIVE -> {
                inactiveApps.add(getPosition(item, inactiveApps), item)
                updateSubscribers(index)
            }
        }
    }

    @Synchronized
    fun updateApp(context: Context, packageName: String) {
        synchronized(this) {
            val currentIndex = getPackageIndex(context, packageName)
            if (contains(packageName) == currentIndex) {
                return
            }
            removeApp(packageName)
            val pInfo =  try {
                context.packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return
            }
            val item = AppItem(packageName = packageName,
                    title = pInfo.applicationInfo.loadLabel(context.packageManager) as String,
                    versionCode = pInfo.getVersionCode(),
                    versionName = pInfo.versionName,
                    icon = pInfo.applicationInfo.loadIcon(context.packageManager),
                    hasUpdate = updates(context).contains(packageName),
                    incompatible = !OverlayUtils.checkVersionCompatible(context, packageName),
                    hasVersions = OverlayUtils.overlayHasVersion(context, packageName),
                    installed = context.swift.romHandler.isOverlayInstalled(packageName),
                    isRequired = context.swift.romHandler.getRequiredApps().contains(packageName),
                    appOptions = OverlayUtils.getOverlayOptions(context, packageName))
            putApp(item, currentIndex)
        }
    }

    @Synchronized
    fun removeApp(packageName: String) {
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