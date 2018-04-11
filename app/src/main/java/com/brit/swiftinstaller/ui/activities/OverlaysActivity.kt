package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.utils.Utils.getOverlayPackageName
import com.brit.swiftinstaller.utils.Utils.isOverlayEnabled
import com.brit.swiftinstaller.utils.Utils.isOverlayFailed
import com.brit.swiftinstaller.utils.Utils.isOverlayInstalled
import com.brit.swiftinstaller.utils.getAccentColor
import com.brit.swiftinstaller.utils.useBlackBackground
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.activity_overlay.*
import kotlinx.android.synthetic.main.toolbar_overlays.*
import kotlinx.android.synthetic.main.tab_layout_overlay.*
import java.lang.ref.WeakReference

class OverlaysActivity : ThemeActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        private const val UPDATE_TAB = 2
        private const val FAILED_TAB = 3
    }

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var mViewPager: ViewPager

    private var mApps: HashMap<Int, ArrayList<AppItem>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        mApps[INSTALL_TAB] = ArrayList()
        mApps[ACTIVE_TAB] = ArrayList()
        mApps[UPDATE_TAB] = ArrayList()
        mApps[FAILED_TAB] = ArrayList()

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = container

        selectAllBtn.setOnClickListener {
            var checked = 0
            mApps[container.currentItem]!!.forEach {
                if (it.checked) checked++
            }
            val check = checked < (mApps[container.currentItem]!!.size / 2)
            for (appItem in mApps[container.currentItem]!!) {
                appItem.checked = check
            }
            mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
        }

        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == FAILED_TAB) {
                    fab.visibility = View.GONE
                } else {
                    fab.visibility = View.VISIBLE
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        currentAccent.setTextColor(getAccentColor(this))
        currentAccent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))

        mApps[INSTALL_TAB]!!.clear()
        mApps[ACTIVE_TAB]!!.clear()
        mApps[UPDATE_TAB]!!.clear()
        mApps[FAILED_TAB]!!.clear()

        AppLoader(this, object : Callback {
            override fun updateApps(tab: Int, item: AppItem) {
                mApps[tab]!!.add(item)
                mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
            }
        }).execute()
    }

    fun customizeBtnClick(view: View) {
        val intent = Intent(this, CustomizeActivity::class.java)
        startActivity(intent)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var mFragments: ArrayList<PlaceholderFragment> = ArrayList()

        init {
            mFragments.add(PlaceholderFragment())
            mFragments[INSTALL_TAB].setAppList(mApps[INSTALL_TAB]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[ACTIVE_TAB].setAppList(mApps[ACTIVE_TAB]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[UPDATE_TAB].setAppList(mApps[UPDATE_TAB]!!)
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
            return mFragments.size
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
            return inflater.inflate(R.layout.activity_app_list, container, false)
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
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
//                var packageName: TextView = view.findViewById(R.id.appName)
                private var appIcon: ImageView = view.findViewById(R.id.appItemImage)
                private var appCheckBox: CheckBox = view.findViewById(R.id.appItemCheckBox)

                fun bindAppItem(item: AppItem) {
                    appName.text = item.title
                    appIcon.setImageDrawable(item.icon)
                    appCheckBox.isChecked = item.checked
//                    packageName.text = item.packageName


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
                    } else if (isOverlayFailed(context, getOverlayPackageName(pn))) {
                        publishProgress(Progress(FAILED_TAB, item))
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
        val intent = Intent(this, InstallActivity::class.java)
        val checked = getCheckedItems(mViewPager.currentItem)
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

    fun alertIconClick(view: View) {
        val dialog = View.inflate(this, R.layout.alert_dialog_error, null)
        val builder = if (useBlackBackground(this)) {
            AlertDialog.Builder(this, R.style.AppAlertDialogTheme_Black).create()
        } else {
            AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
        }
        val closeBtn = dialog.findViewById<View>(R.id.errorCloseBtn)
        builder.setView(dialog)
        closeBtn.setOnClickListener {
            builder.dismiss()
        }
        builder.show()
    }
}
