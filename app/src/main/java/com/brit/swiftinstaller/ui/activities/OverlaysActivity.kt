package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.*
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.Utils.getOverlayPackageName
import com.brit.swiftinstaller.utils.Utils.isOverlayEnabled
import com.brit.swiftinstaller.utils.Utils.isOverlayInstalled
import com.brit.swiftinstaller.utils.getAccentColor
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
                        .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                dialog.show()
            }

        })

        mViewPager = container

        selectAllBtn.setOnClickListener {
            val checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
            val check = checked < (mPagerAdapter!!.getAppsCount(container.currentItem) / 2)
            mPagerAdapter!!.selectAll(container.currentItem, check)
            mPagerAdapter!!.notifyFragmentDataSetChanged(container.currentItem)
        }

        container.adapter = mPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        if (intent.hasExtra("tab")) {
            mViewPager.currentItem = intent.getIntExtra("tab", 0)
        }
    }

    override fun onResume() {
        super.onResume()
        currentAccent.setTextColor(getAccentColor(this))
        currentAccent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))

        mPagerAdapter!!.clearApps()

        AppLoader(this, object : Callback {
            override fun updateApps(tab: Int, item: AppItem) {
                mPagerAdapter!!.addApp(tab, item)
            }
        }).execute()
    }

    fun customizeBtnClick(view: View) {
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
            for (pn: String in mConRef.get()!!.assets.list("overlays")) {
                var info: ApplicationInfo? = null
                var pInfo: PackageInfo? = null
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
                    if (isOverlayInstalled(context!!, getOverlayPackageName(pn))) {
                        if (isOverlayEnabled(context, getOverlayPackageName(pn))) {
                            publishProgress(Progress(ACTIVE_TAB, item))
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

    fun fabClick(view: View) {
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_overlays_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.install)
        install.setOnClickListener {
            bottomSheetDialog.dismiss()
            installAction()
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
            container.currentItem == ACTIVE_TAB -> install.visibility = View.GONE
            container.visibility == UPDATE_TAB -> install.visibility == View.GONE
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

        val uninstall = sheetView.findViewById<View>(R.id.confirmUninstallTxt)
        uninstall.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallProgressAction()
        }

        val cancel = sheetView.findViewById<View>(R.id.cancelUninstallTxt)
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
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_update_progress, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    fun overlaysBackClick(view: View) {
        onBackPressed()
    }
}
