package com.brit.swiftinstaller.installer.rom

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.*
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import java.io.File


@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
open class RomInfo constructor(var context: Context) {

    var defaultAccent: Int = 0

    init {
        defaultAccent = context.getColor(R.color.minimal_blue)
    }

    open fun getDisabledOverlays(): ArrayList<String> {
        val disable = ArrayList<String>()
        disable.add("com.android.emergency")
        return disable
    }

    open fun getRequiredApps(): Array<String> {
        return Array(29) {
            when (it) {
                0 -> "android"
                1 -> "com.android.systemui"
                2 -> "com.amazon.clouddrive.photos"
                3 -> "com.android.settings"
                4 -> "com.android.systemui"
                5 -> "com.anydo"
                6 -> "com.apple.android.music"
                7 -> "com.ebay.mobile"
                8 -> "com.embermitre.pixolor.app"
                9 -> "com.google.android.apps.genie.geniewidget"
                10 -> "com.google.android.apps.inbox"
                11 -> "com.google.android.apps.messaging"
                12 -> "com.google.android.gm"
                13 -> "com.google.android.talk"
                14 -> "com.mxtech.videoplayer.ad"
                15 -> "com.mxtech.videoplayer.pro"
                16 -> "com.pandora.android"
                17 -> "com.simplecity.amp.pro"
                18 -> "com.Slack"
                19 -> "com.samsung.android.incallui"
                20 -> "com.twitter.android"
                21 -> "com.samsung.android.contacts"
                22 -> "com.samsung.android.scloud"
                23 -> "com.samsung.android.themestore"
                24 -> "com.samsung.android.lool"
                25 -> "com.samsung.android.samsungpassautofill"
                26 -> "com.google.android.gms"
                27 -> "com.sec.android.daemonapp"
                28 -> "de.axelspringer.yana.zeropage"
                else -> ""
            }
        }
    }

    open fun addTutorialSteps(tutorial : TutorialActivity) {
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.app_name))
                .setContent(tutorial.getString(R.string.tutorial_guide))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_logo) // int top drawable
                .build())
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.tutorial_apps_title))
                .setContent(tutorial.getString(R.string.tutorial_apps))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_apps) // int top drawable
                .build())
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.basic_usage))
                .setContent(tutorial.getString(R.string.tutorial_basic_usage_content))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_hand) // int top drawable
                .build())
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.tutorial_more_usage_title))
                .setContent(tutorial.getString(R.string.tutorial_more_usage_info))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_clicks) // int top drawable
                .build())
        tutorial.addFragment(PermissionStep.Builder().setTitle(tutorial.getString(R.string.tutorial_permission_title))
                .setContent(tutorial.getString(R.string.tutorial_permission_content))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main)) // int background color
                .setDrawable(R.drawable.ic_tutorial_permission)
                .setPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .build())
        tutorial.addFragment(Step.Builder().setTitle(tutorial.getString(R.string.tutorial_customize_title))
                .setContent(tutorial.getString(R.string.tutorial_customize_content))
                .setBackgroundColor(ContextCompat.getColor(tutorial, R.color.background_main))
                .setDrawable(R.drawable.ic_tutorial_customize) // int top drawable
                .build())
    }

    open fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    open fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
        val extraIntent = intent != null

        if (ShellUtils.isRootAvailable) {
            return
        }

        if (apps.contains("android")) {
            val index = apps.indexOf("android")
            apps.removeAt(index)
            apps.add(0, "android")
        }
        if (apps.contains("com.google.android.packageinstaller")) {
            val index = apps.indexOf("com.google.android.packageinstaller")
            apps.removeAt(index)
            apps.add(0, "com.google.android.packageinstaller")
        }

        val intents = Array(if (!extraIntent) {
            apps.size
        } else {
            apps.size + 1
        }) { i ->
            val index = if (extraIntent) {
                i - 1
            } else {
                i
            }
            if (!extraIntent || i > 0) {
                val appInstall = Intent()
                if (uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            Utils.getOverlayPackageName(apps.elementAt(index)), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".myprovider",
                            File(Utils.getOverlayPath(apps.elementAt(index))))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            } else {
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
        }

        if (!intents.isEmpty()) {
            context.startActivities(intents)
        }

        if (oppositeApps != null && !oppositeApps.isEmpty()) {
            val oppositeIntents = Array(oppositeApps.size) {
                val appInstall = Intent()
                if (!uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            Utils.getOverlayPackageName(oppositeApps[it]), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".myprovider",
                            File(Utils.getOverlayPath(oppositeApps[it])))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            }
            context.startActivities(oppositeIntents)
        }

        clearAppsToUninstall(context)
        clearAppsToInstall(context)
    }

    open fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + Utils.getOverlayPackageName(packageName), true)
        } else {
            addAppToUninstall(context, Utils.getOverlayPackageName(packageName))
        }
    }

    @Suppress("unused")
    companion object {

        @SuppressLint("StaticFieldLeak")
        private var sInfo: RomInfo? = null

        @Synchronized
        @JvmStatic
        fun getRomInfo(context: Context): RomInfo {
            if (sInfo == null) {
                sInfo = when {
                    Build.VERSION_CODES.P == Build.VERSION.SDK_INT -> PRomInfo(context)
                    Utils.isSamsungOreo(context) -> RomInfo(context)
                    System.getProperty("ro.oxygenos.version", "def") != "def" -> OOSRomInfo(context)
                    else -> OreoRomInfo(context)
                }
            }
            return sInfo!!
        }

        private val isTouchwiz: Boolean
            get() = File("/system/framework/touchwiz.jar").exists()

        @Suppress("DEPRECATION", "unused")
        private fun isOMS(context: Context): Boolean {
            val am = context.getSystemService(ActivityManager::class.java)!!
            val services = am.getRunningServices(Integer.MAX_VALUE)
            for (info in services) {
                if (info.service.className.contains("IOverlayManager")) {
                    return true
                }
            }
            return false
        }

        fun isSupported(@Suppress("UNUSED_PARAMETER") context: Context): Boolean {
            return true
        }
    }
}
