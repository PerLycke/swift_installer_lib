package com.brit.swiftinstaller.installer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.installer.rom.RomInfo
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

    private val compileQueue: LinkedBlockingQueue<Runnable>
    private val overlayQueue: Queue<OverlayTask>

    private var callback: Callback? = null

    private var mMax = 0

    private val mThreadPool: ThreadPoolExecutor

    private val mHandler: Handler

    private val mNotifier = Notifier()
    private val mRomInfo = RomInfo.getRomInfo(context)

    init {

        compileQueue = LinkedBlockingQueue()
        overlayQueue = LinkedBlockingQueue<OverlayTask>()

        mThreadPool = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, compileQueue)

        mHandler = object : Handler(Looper.myLooper()) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)

                val overlayTask = msg!!.obj as OverlayTask

                when (msg.what) {
                    OVERLAY_FAILED -> {
                        Notifier.broadcastOverlayFailed(context,
                                overlayTask.packageName, overlayTask.errorLog)
                    }

                    OVERLAY_INSTALLED -> {
                        if (msg.arg1 == (msg.arg2 - 1)) {
                            if (callback != null) {
                                callback!!.installFinished()
                                Notifier.broadcastInstallFinished(context)
                            }
                        }
                        Notifier.broadcastOverlayInstalled(context, overlayTask.packageName, overlayTask.index, msg.arg2)
                    }

                    OVERLAY_UNINSTALLED -> {
                        if (msg.arg1 == (msg.arg2 - 1)) {
                            if (callback != null) {
                                callback!!.installFinished()
                            }
                            Notifier.broadcastUninstallFinished(context)
                        }
                        Notifier.broadcastOverlayUninstalled(context, overlayTask.packageName, overlayTask.index, msg.arg2)
                    }
                }
            }
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun installOverlays(apps: Array<String>) {
        mMax = apps.size
        //mNotifier.broadcastInstallStarted(mMax)
        for (pn in apps) {
            installOverlay(pn, apps.indexOf(pn))
        }
    }

    fun uninstallOverlays(apps: Array<String>) {
        mMax = apps.size
        //mNotifier.broadcastInstallStarted(mMax)
        for (pn in apps) {
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

        mThreadPool.execute(task.getRunnable())
    }

    fun isRunning(): Boolean {
        return mThreadPool.queue.isEmpty()
    }

    private fun uninstallOverlay(packageName: String, index: Int) {
        var task = overlayQueue.poll()
        if (task == null) {
            task = (OverlayTask(this))
        }

        task.initializeOverlayTask(context, packageName, index, true)
        mThreadPool.execute(task.getRunnable())
    }

    fun handleState(task: OverlayTask, state: Int) {
        when (state) {
            OVERLAY_INSTALLED -> {
                val installed = mHandler.obtainMessage(state, task)
                installed.arg1 = task.index
                installed.arg2 = mMax
                installed.sendToTarget()
            }
            OVERLAY_UNINSTALLED -> {
                val uninstalled = mHandler.obtainMessage(state, task)
                uninstalled.arg1 = task.index
                uninstalled.arg2 = mMax
                uninstalled.sendToTarget()
            }

            OVERLAY_FAILED -> {
                val failed = mHandler.obtainMessage(state, task)
                failed.arg1 = task.index
                failed.sendToTarget()
            }
        }
    }
}