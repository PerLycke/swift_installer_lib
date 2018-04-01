package com.brit.swiftinstaller.installer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.brit.swiftinstaller.IInstallerCallback
import com.brit.swiftinstaller.utils.rom.RomInfo
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class OverlayManager(val mContext: Context) {

    companion object {
        const val OVERLAY_FAILED = -1
        const val OVERLAY_COMPILING = 1
        const val OVERLAY_INSTALLING = 2
        const val OVERLAY_INSTALLED = 3
        const val OVERLAY_UNINSTALLED = 4

        private const val KEEP_ALIVE_TIME: Long = 1
        private const val CORE_POOL_SIZE = 2
        private const val MAX_POOL_SIZE = 4

        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    }

    private val mCompileQueue: BlockingQueue<Runnable>
    private val mOverlayQueue: Queue<OverlayTask>

    private var mCallback: IInstallerCallback? = null

    var mMax = 0

    private val mThreadPool: ThreadPoolExecutor

    private val mHandler: Handler

    private val mNotifier = Notifier(mContext)

    init {

        mCompileQueue = LinkedBlockingQueue<Runnable>()
        mOverlayQueue = LinkedBlockingQueue<OverlayTask>()

        mThreadPool = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, mCompileQueue)

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)

                val overlayTask = msg!!.obj as OverlayTask

                when (msg.what) {
                    OVERLAY_FAILED -> {
                        mNotifier.broadcastOverlayFailed(overlayTask.packageName, msg.arg1)
                    }

                    OVERLAY_INSTALLED -> {
                        mCallback!!.progressUpdate(mContext.packageManager
                                .getApplicationInfo(overlayTask.packageName, 0)
                                .loadLabel(mContext.packageManager) as String,
                                msg.arg1, msg.arg2, false)
                        mNotifier.broadcastOverlayInstalled(overlayTask.packageName, msg.arg1, msg.arg2)
                    }

                    OVERLAY_UNINSTALLED -> {
                        mCallback!!.progressUpdate(mContext.packageManager
                                .getApplicationInfo(overlayTask.packageName, 0)
                                .loadLabel(mContext.packageManager) as String,
                                msg.arg1, msg.arg2, true)
                    }
                }
            }
        }
    }

    fun setCallback(callback: IInstallerCallback) {
        mCallback = callback
    }

    fun installOverlays(apps: List<String>) {
        mMax = apps.size - 1
        mNotifier.broadcastInstallStarted(mMax)
        for (pn in apps) {
            installOverlay(pn, apps.indexOf(pn))
        }
    }

    fun uninstallOverlays(apps: List<String>) {
        mMax = apps.size - 1
        mNotifier.broadcastInstallStarted(mMax)
        for (pn in apps) {
            uninstallOverlay(pn, apps.indexOf(pn))

        }
    }

    private fun installOverlay(packageName: String, index: Int) {
        var task = mOverlayQueue.poll()
        if (task == null) {
            task = OverlayTask(this)
        }

        task.initializeOverlayTask(mContext, packageName, index, false)

        mThreadPool.execute(task.getRunnable())
    }

    private fun uninstallOverlay(packageName: String, index: Int) {
        var task = mOverlayQueue.poll()
        if (task == null) {
            task = (OverlayTask(this))
        }

        task.initializeOverlayTask(mContext, packageName, index, true)
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
        }
    }
}