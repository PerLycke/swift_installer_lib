/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library.installer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.brit.swiftinstaller.library.ui.applist.AppList
import com.brit.swiftinstaller.library.utils.Utils
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class OverlayManager(private val context: Context) {

    companion object {
        const val OVERLAY_FAILED = -1
        const val OVERLAY_INSTALLED = 2
        const val OVERLAY_UNINSTALLED = 3

        private const val KEEP_ALIVE_TIME: Long = 1
        private const val CORE_POOL_SIZE = 1
        private const val MAX_POOL_SIZE = 1
    }

    interface Callback {
        fun installFinished()
    }

    private val compileQueue = LinkedBlockingQueue<Runnable>()
    private val overlayQueue: Queue<OverlayTask>

    private var callback: Callback? = null

    private var max = 0

    private val threadPool: ThreadPoolExecutor

    private val handler: Handler

    init {

        overlayQueue = LinkedBlockingQueue<OverlayTask>()

        threadPool = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, compileQueue)

        handler = object : Handler(Looper.myLooper()) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)

                val overlayTask = msg!!.obj as OverlayTask

                when (msg.what) {
                    OVERLAY_FAILED -> {
                        Notifier.broadcastOverlayFailed(context,
                                overlayTask.packageName, overlayTask.errorLog)
                    }

                    OVERLAY_INSTALLED -> {
                        AppList.addApp(context, overlayTask.packageName)
                        Notifier.broadcastOverlayInstalled(context, overlayTask.packageName,
                                overlayTask.index, msg.arg2)
                        if (msg.arg1 == (msg.arg2 - 1)) {
                            if (callback != null) {
                                callback!!.installFinished()
                                Notifier.broadcastInstallFinished(context)
                            }
                        }
                    }

                    OVERLAY_UNINSTALLED -> {
                        AppList.addApp(context, overlayTask.packageName)
                        if (msg.arg1 == (msg.arg2 - 1)) {
                            if (callback != null) {
                                callback!!.installFinished()
                            }
                            Notifier.broadcastUninstallFinished(context)
                        }
                        Notifier.broadcastOverlayUninstalled(context, overlayTask.packageName,
                                overlayTask.index, msg.arg2)
                    }
                }
            }
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun installOverlays(apps: Array<String>) {
        max = apps.size
        apps.forEach { pn ->
            installOverlay(pn, apps.indexOf(pn))
        }
    }

    fun uninstallOverlays(apps: Array<String>) {
        max = apps.size
        //mNotifier.broadcastInstallStarted(max)
        apps.forEach { pn ->
            uninstallOverlay(pn, apps.indexOf(pn))
        }
    }

    private fun installOverlay(packageName: String, index: Int) {
        var task = overlayQueue.poll()
        if (task == null) {
            task = OverlayTask(this)
        }

        if (Utils.isAppInstalled(context, packageName)) {
            task.initializeOverlayTask(context, packageName, index, false)
        }

        threadPool.execute(task.getRunnable())
    }

    fun isRunning(): Boolean {
        return threadPool.queue.isEmpty()
    }

    private fun uninstallOverlay(packageName: String, index: Int) {
        var task = overlayQueue.poll()
        if (task == null) {
            task = (OverlayTask(this))
        }

        task.initializeOverlayTask(context, packageName, index, true)
        threadPool.execute(task.getRunnable())
    }

    fun handleState(task: OverlayTask, state: Int) {
        when (state) {
            OVERLAY_INSTALLED -> {
                val installed = handler.obtainMessage(state, task)
                installed.arg1 = task.index
                installed.arg2 = max
                installed.sendToTarget()
            }
            OVERLAY_UNINSTALLED -> {
                val uninstalled = handler.obtainMessage(state, task)
                uninstalled.arg1 = task.index
                uninstalled.arg2 = max
                uninstalled.sendToTarget()
            }

            OVERLAY_FAILED -> {
                val failed = handler.obtainMessage(state, task)
                failed.arg1 = task.index
                failed.sendToTarget()
            }
        }
    }
}