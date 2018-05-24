package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.Utils.getOverlayPackageName
import com.brit.swiftinstaller.utils.Utils.isOverlayEnabled
import com.brit.swiftinstaller.utils.Utils.isOverlayInstalled
import kotlinx.android.synthetic.main.activity_overlays.*
import kotlinx.android.synthetic.main.tab_layout_overlay.*
import kotlinx.android.synthetic.main.tab_overlays_updates.*
import kotlinx.android.synthetic.main.toolbar_overlays.*
import java.lang.ref.WeakReference

class OverlaysActivity : ThemeActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        const val UPDATE_TAB = 2

        private val requiredApps = Array(28, {
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
                else -> ""
            }
        })
    }

    private var mPagerAdapter: AppsTabPagerAdapter? = null
    private lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlays)

        mPagerAdapter = AppsTabPagerAdapter(supportFragmentManager,
                false, INSTALL_TAB, ACTIVE_TAB, UPDATE_TAB)
        mPagerAdapter!!.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                val packageInfo = packageManager.getPackageInfo(appItem.packageName, 0)
                val dialog = AlertDialog.Builder(this@OverlaysActivity, R.style.AppTheme_AlertDialog)

                themeDialog()

                dialog.setTitle(appItem.title)
                dialog.setIcon(appItem.icon)
                dialog.setMessage("Version support info:" +
                        "\nCurrent Version: ${packageInfo.versionName}" +
                        "\nAvailable Versions: ${Utils.getAvailableOverlayVersions(
                                this@OverlaysActivity, appItem.packageName)}")
                dialog.setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                dialog.show()
            }
        })
        mPagerAdapter!!.setRequiredApps(INSTALL_TAB, requiredApps)

        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                if (updates.isEmpty()) {
                    update_tab_indicator.visibility = View.GONE
                } else {
                    update_tab_indicator.visibility = View.VISIBLE
                }
            }

        }).execute()

        search_view.setOnSearchClickListener {
            toolbar_overlays_main_content.visibility = View.GONE
        }
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mPagerAdapter!!.querySearch(container.currentItem, newText!!)
                return true
            }
        })
        search_view.setOnCloseListener {
            toolbar_overlays_main_content.visibility = View.VISIBLE
            false
        }

        mViewPager = container

        select_all_btn.setOnClickListener {
            val checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
            val check = checked < (mPagerAdapter!!.getAppsCount(container.currentItem) / 2)
            mPagerAdapter!!.selectAll(container.currentItem, check)
            mPagerAdapter!!.notifyFragmentDataSetChanged(container.currentItem)
        }

        container.adapter = mPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs_overlays_root))
        tabs_overlays_root.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val checked = mPagerAdapter!!.getCheckedCount(position)
                select_all_btn.isChecked =
                        checked == mPagerAdapter!!.getAppsCount(position) && checked > 0
            }

        })

        if (intent.hasExtra("tab")) {
            mViewPager.currentItem = intent.getIntExtra("tab", 0)
        }
    }

    override fun onResume() {
        super.onResume()
        toolbar_subtitle_current_accent.setTextColor(getAccentColor(this))
        toolbar_subtitle_current_accent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))
        toolbar_subtitle_current_bg.text = getString(R.string.hex_string,
                String.format("%06x", getBackgroundColor(this)).substring(2))

        mPagerAdapter!!.clearApps()

        AppLoader(this, object : Callback {
            override fun updateApps(tab: Int, item: AppItem) {
                mPagerAdapter!!.addApp(tab, item)
            }
        }).execute()
    }

    fun customizeBtnClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, CustomizeActivity::class.java)
        startActivity(intent)
    }

    interface Callback {
        fun updateApps(tab: Int, item: AppItem)
    }

    private fun getCheckedItems(index: Int): ArrayList<AppItem> {
        return mPagerAdapter!!.getCheckedItems(index)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class AppLoader(context: Context, private val mCallback: Callback)
        : AsyncTask<Void, AppLoader.Progress, Void>() {

        private val mConRef: WeakReference<Context> = WeakReference(context)

        class Progress(val tab: Int, val item: AppItem)

        override fun doInBackground(vararg params: Void?): Void? {
            assert(mConRef.get() != null)
            val pm = mConRef.get()!!.packageManager
            val context = mConRef.get()
            val updates = getAppsToUpdate(context!!)
            for (pn: String in mConRef.get()!!.assets.list("overlays")) {
                var info: ApplicationInfo?
                var pInfo: PackageInfo?
                var status: Int?
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                    status = pm.getApplicationEnabledSetting(pn)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
                if (info != null) {
                    val item = AppItem()
                    item.packageName = pn
                    item.icon = info.loadIcon(pm)
                    item.title = info.loadLabel(pm) as String
                    item.versionCode = pInfo!!.versionCode
                    item.versionName = pInfo.versionName
                    if (isOverlayInstalled(context, getOverlayPackageName(pn))) {
                        if (isOverlayEnabled(context, getOverlayPackageName(pn))) {
                            if (updates.contains(pn)
                                    && status != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                                publishProgress(Progress(UPDATE_TAB, item))
                            } else {
                                publishProgress(Progress(ACTIVE_TAB, item))
                            }
                        } else if (status != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                            publishProgress(Progress(INSTALL_TAB, item))
                        }
                    } else if (status != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                        publishProgress(Progress(INSTALL_TAB, item))
                    }
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg progress: Progress?) {
            super.onProgressUpdate(*progress)
            mCallback.updateApps(progress[0]!!.tab, progress[0]!!.item)
        }

    }

    fun fabClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_overlays_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.window.decorView.findViewById<View>(R.id.design_bottom_sheet).setBackgroundColor(getBackgroundColor(this))
        bottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.install)
        install.setOnClickListener {
            val launch = getSharedPreferences("launched", Context.MODE_PRIVATE).getString("launched","first")
            bottomSheetDialog.dismiss()

            when (launch) {
                "default" -> installAction()
                "first" -> {
                    getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "second").apply()
                    installAction()
                }
                "second" -> {
                    val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                    themeDialog()
                    builder.setTitle(R.string.reboot_delay_title)
                    builder.setMessage(R.string.reboot_delay_msg)
                    builder.setPositiveButton(R.string.proceed, { dialogInterface, _ ->
                        getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "default").apply()
                        dialogInterface.dismiss()
                        installAction()
                    })
                    builder.setNegativeButton(R.string.cancel, { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    })

                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }

        val uninstall = sheetView.findViewById<View>(R.id.uninstall)
        uninstall.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallAction()
        }

        val update = sheetView.findViewById<View>(R.id.update)
        update.setOnClickListener {
            bottomSheetDialog.dismiss()
            updateAction()
        }

        when {
            container.currentItem == INSTALL_TAB -> {
                uninstall.visibility = View.GONE
                update.visibility = View.GONE
            }
            container.currentItem == ACTIVE_TAB -> {
                install.visibility = View.GONE
                val checked = getCheckedItems(ACTIVE_TAB)
                val updates = getAppsToUpdate(this)
                var updatesAvailable = false
                checked.forEach {
                    if (!updatesAvailable)
                        updatesAvailable = updates.contains(it.packageName)
                }
                update.visibility = if (updatesAvailable) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            container.currentItem == UPDATE_TAB -> {
                install.visibility = View.GONE
            }
        }
    }

    private fun installAction() {
        val checked = getCheckedItems(mViewPager.currentItem)
        if (checked.isEmpty()) {
            return
        }
        val intent = Intent(this, InstallActivity::class.java)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        startActivity(intent)
    }

    private fun uninstallAction() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_confirm_uninstall, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.window.decorView.findViewById<View>(R.id.design_bottom_sheet).setBackgroundColor(getBackgroundColor(this))
        bottomSheetDialog.show()

        val uninstall = sheetView.findViewById<View>(R.id.confirm_uninstall_txt)
        uninstall.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallProgressAction()

            Toast.makeText(this, "This can take a lot of time, have patience!", Toast.LENGTH_LONG).show()
        }

        val cancel = sheetView.findViewById<View>(R.id.cancel_uninstall_txt)
        cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun uninstallProgressAction() {
        val intent = Intent(this, InstallActivity::class.java)
        val checked = getCheckedItems(mViewPager.currentItem)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("uninstall", true)
        startActivity(intent)
    }

    private fun updateAction() {
        val checked = getCheckedItems(mViewPager.currentItem)
        if (checked.isEmpty()) {
            return
        }
        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                mPagerAdapter!!.clearApps()
                AppLoader(this@OverlaysActivity, object : Callback {
                    override fun updateApps(tab: Int, item: AppItem) {
                        mPagerAdapter!!.addApp(tab, item)
                    }
                }).execute()
            }

        }).execute()
        val intent = Intent(this, InstallActivity::class.java)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("update", true)
        startActivity(intent)
    }

    fun overlaysBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressed()
    }
}
