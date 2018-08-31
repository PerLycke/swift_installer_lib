package com.brit.swiftinstaller.installer.rom

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.ui.activities.CustomizeActivity
import com.brit.swiftinstaller.utils.*
import com.hololo.tutorial.library.PermissionStep
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity
import java.io.File


@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
abstract class RomInfo constructor(var context: Context) {

    open fun getDefaultAccent() : Int {
        return context.getColor(R.color.minimal_blue)
    }

    open fun getDisabledOverlays(): ArrayList<String> {
        val disable = ArrayList<String>()
        disable.add("com.android.emergency")
        return disable
    }

    open fun getRequiredApps(): Array<String> {
        return emptyArray()
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

    open fun isOverlayInstalled(targetPackage: String): Boolean {
        return Utils.isAppInstalled(context, Utils.getOverlayPackageName(targetPackage))
    }

    open fun disableOverlay(targetPackage: String) {
    }

    open fun getCustomizeFeatures() : Int {
        return CustomizeActivity.SUPPORTS_CLOCK + CustomizeActivity.SUPPORTS_ICONS + CustomizeActivity.SUPPORTS_SYSTEMUI
    }

    open fun needsSecondReboot(): Boolean { return true }

    abstract fun installOverlay(context: Context, targetPackage: String, overlayPath: String)
    abstract fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?)
    abstract fun uninstallOverlay(context: Context, packageName: String)

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
                    Utils.isSamsungOreo(context) -> SamsungRomInfo(context)
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
