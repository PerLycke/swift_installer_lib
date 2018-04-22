package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.Utils.getOverlayPackageName
import com.brit.swiftinstaller.utils.Utils.isOverlayEnabled
import com.brit.swiftinstaller.utils.Utils.isOverlayInstalled
import kotlinx.android.synthetic.main.activity_overlays.*
import kotlinx.android.synthetic.main.toolbar_overlays.*
import kotlinx.android.synthetic.main.tab_layout_overlay.*
import java.lang.ref.WeakReference

class OverlaysActivity : ThemeActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        const val UPDATE_TAB = 2
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
                val dialog = AlertDialog.Builder(this@OverlaysActivity)
                        .setTitle(appItem.title)
                        .setIcon(appItem.icon)
                        .setMessage("Version Unsupported." +
                                "\nCurrent Version: ${packageInfo.versionName}" +
                                "\nAvailable Versions: ${Utils.getAvailableOverlayVersions(
                                        this@OverlaysActivity, appItem.packageName)}")
                        .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                dialog.show()
            }

        })

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
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
                if (info != null) {
                    val item = AppItem()
                    item.packageName = pn
                    item.icon = info.loadIcon(pm)
                    item.title = info.loadLabel(pm) as String
                    item.version = pInfo!!.versionCode
                    if (isOverlayInstalled(context, getOverlayPackageName(pn))) {
                        if (isOverlayEnabled(context, getOverlayPackageName(pn))) {
                            if (updates.contains(pn)) {
                                publishProgress(Progress(UPDATE_TAB, item))
                            } else {
                                publishProgress(Progress(ACTIVE_TAB, item))
                            }
                        } else {
                            publishProgress(Progress(INSTALL_TAB, item))
                        }
                    } else {
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
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_overlays_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.install)
        install.setOnClickListener {
            if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_time", true)) {
                bottomSheetDialog.dismiss()
                installAction()
            } else {
                bottomSheetDialog.dismiss()
                val builder: AlertDialog.Builder
                builder = if (AppCompatDelegate.getDefaultNightMode()
                        == AppCompatDelegate.MODE_NIGHT_YES) {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog_Black)
                } else {
                    AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                }
                        .setTitle(R.string.installing_and_uninstalling_title)
                        .setMessage(R.string.installing_and_uninstalling_msg)
                        .setPositiveButton(R.string.proceed, { dialogInterface, _ ->
                            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_time", false).apply()
                            dialogInterface.dismiss()
                            installAction()
                        })

                val dialog = builder.create()
                dialog.show()
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
                update.visibility = if (updatesAvailable) { View.VISIBLE } else { View.GONE }
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
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_confirm_uninstall, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        val uninstall = sheetView.findViewById<View>(R.id.confirm_uninstall_txt)
        uninstall.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallProgressAction()

            val toast = Toast.makeText(this, "This can take a lot of time, have patience!", Toast.LENGTH_LONG)
            val view = toast.view

            val text = view.findViewById<TextView>(android.R.id.message)
            text.setTextColor(ContextCompat.getColor(this, R.color.minimal_green))

            if (AppCompatDelegate.getDefaultNightMode()
                    == AppCompatDelegate.MODE_NIGHT_YES) {
                view.setBackgroundResource(R.drawable.toast_black)
            } else {
                view.setBackgroundResource(R.drawable.toast_dark)
            }

            toast.show()
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
