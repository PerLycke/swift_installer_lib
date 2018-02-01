package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.Utils.getOverlayPackageName
import com.brit.swiftinstaller.utils.Utils.isOverlayEnabled
import com.brit.swiftinstaller.utils.Utils.isOverlayInstalled
import kotlinx.android.synthetic.main.app_list_activity.*
import kotlinx.android.synthetic.main.overlay_activity.*
import kotlinx.android.synthetic.main.tab_layout.*
import java.lang.ref.WeakReference

class OverlayActivity : AppCompatActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        private const val FAILED_TAB = 2
    }

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mApps: HashMap<Int, ArrayList<AppItem>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overlay_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mApps[0] = ArrayList()
        mApps[1] = ArrayList()
        mApps[2] = ArrayList()

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        AppLoader(this, object : Callback {
            override fun updateApps(tab: Int, item: AppItem) {
                mApps[tab]!!.add(item)
                mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
            }
        }).execute()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu!!.add(0, 1, 0, "Select All").setIcon(R.drawable.select_all).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var mFragments: ArrayList<PlaceholderFragment> = ArrayList()

        init {
            mFragments.add(PlaceholderFragment())
            mFragments[INSTALL_TAB].setAppList(mApps[INSTALL_TAB]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[ACTIVE_TAB].setAppList(mApps[ACTIVE_TAB]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[FAILED_TAB].setAppList(mApps[FAILED_TAB]!!)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        fun notifyFragmentDataSetChanged() {
            for (i in mFragments.indices) {
                mFragments[i].setAppList(mApps[i]!!)
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }


    class AppItem {
        var packageName: String = ""
        var title: String = ""
        var version: Int = 0
        var icon: Drawable? = null
        var checked: Boolean = false
    }

    interface Callback {
        fun updateApps(tab: Int, item: AppItem)
    }

    class PlaceholderFragment : Fragment() {

        var mApps: ArrayList<AppItem> = ArrayList()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.app_list_activity, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            appListView.adapter = AppAdapter()
            appListView.layoutManager = LinearLayoutManager(activity)
        }

        fun setAppList(apps: ArrayList<AppItem>) {
            mApps.clear()
            mApps.addAll(apps)
            if (appListView != null) {
                appListView.adapter.notifyDataSetChanged()
            }
        }

        inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
                return ViewHolder(LayoutInflater.from(activity).inflate(
                        R.layout.app_item, parent, false))
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.bindAppItem(mApps[position])
            }

            override fun getItemCount(): Int {
                return mApps.size
            }

            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                private var appName: TextView = view.findViewById(R.id.appItemName)
                var packageName: TextView = view.findViewById(R.id.appName)
                private var appIcon: ImageView = view.findViewById(R.id.appItemImage)
                private var appCheckBox: CheckBox = view.findViewById(R.id.appItemCheckBox)

                fun bindAppItem(item: AppItem) {
                    appName.text = item.title
                    appIcon.setImageDrawable(item.icon)
                    appCheckBox.isChecked = item.checked
                    packageName.text = item.packageName


                    appCheckBox.setOnCheckedChangeListener({ _: CompoundButton, checked: Boolean ->
                        item.checked = checked
                    })
                }
            }

        }
    }

    private fun getCheckedItems(index: Int): ArrayList<AppItem> {
        val checked = ArrayList<AppItem>()
        mApps[index]!!.filterTo(checked) { it.checked }
        return checked
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            1 -> {
                for (appItem in mApps[container.currentItem]!!) {
                    appItem.checked = true
                }
                mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
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
                    } else if (isOverlayInstalled(context, pn)) {
                        publishProgress(Progress(INSTALL_TAB, item))
                    }
                    publishProgress()
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
        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = View.inflate(this, R.layout.fab_actions_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.installTxt)
        install.setOnClickListener {
            mBottomSheetDialog.dismiss()
            installAction()
        }

        val uninstall = sheetView.findViewById<View>(R.id.uninstallTxt)
        uninstall.setOnClickListener {
            mBottomSheetDialog.dismiss()
            uninstallAction()
        }

        val update = sheetView.findViewById<View>(R.id.updateTxt)
        update.setOnClickListener {
            mBottomSheetDialog.dismiss()
            updateAction()
        }
    }

    private fun installAction() {
        val intent = Intent(this, InstallActivity::class.java)
        val checked = getCheckedItems(INSTALL_TAB)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        startActivity(intent)
    }

    private fun uninstallAction() {
        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = LayoutInflater.from(this).inflate(R.layout.confirm_uninstall_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()

        val uninstall = sheetView.findViewById<View>(R.id.confirmUninstallTxt)
        uninstall.setOnClickListener {
            mBottomSheetDialog.dismiss()
            uninstallProgressAction()
        }

        val cancel = sheetView.findViewById<View>(R.id.cancelUninstallTxt)
        cancel.setOnClickListener {
            mBottomSheetDialog.dismiss()
        }
    }

    private fun uninstallProgressAction() {
        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = View.inflate(this, R.layout.uninstall_progress_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
    }

    private fun updateAction() {
        val mBottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = View.inflate(this, R.layout.update_progress_sheet, null)
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
    }

    fun alertIconClick(view: View) {
        val dialog = View.inflate(this, R.layout.error_dialog, null)
        val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
        val closeBtn = dialog.findViewById<View>(R.id.closeBtn)
        builder.setView(dialog)
        closeBtn.setOnClickListener {
            builder.dismiss()
        }
        builder.show()
    }
}
