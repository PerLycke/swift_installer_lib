package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.ui.fragments.AppListFragment
import com.brit.swiftinstaller.utils.Utils
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_install_summary.*
import kotlinx.android.synthetic.main.sheet_install_summary_fab.view.*
import kotlinx.android.synthetic.main.tab_layout_install_summary.*
import java.lang.ref.WeakReference

class InstallSummaryActivity : AppCompatActivity() {

    companion object {
        private const val SUCCESS_TAB = 0
        private const val FAILED_TAB = 1
    }

    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    private var mApps: HashMap<Int, ArrayList<OverlaysActivity.AppItem>> = HashMap()
    private var mErrorMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_summary)

        mApps[SUCCESS_TAB] = ArrayList()
        mApps[FAILED_TAB] = ArrayList()

        if (intent.extras.containsKey("errorMap")) {
            mErrorMap = Utils.bundleToMap(intent.getBundleExtra("errorMap"))
        }

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val apps = intent.getStringArrayListExtra("apps")

        AppLoader(this, apps, mErrorMap, object : OverlaysActivity.Callback {
            override fun updateApps(tab: Int, item: OverlaysActivity.AppItem) {
                mApps[tab]!!.add(item)
                mSectionsPagerAdapter.notifyFragmentDataSetChanged(tab)
            }
        }).execute()
    }

    fun rebootActions(view: View) {
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_install_summary_fab, null)

        sheetView.reboot.setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            bottomSheetDialog.dismiss()
            pm.reboot(null)
        }

        sheetView.sendLog.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, Array(1, { "swiftuserhelp@gmail.com" }))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Swift Installer: Error Log")

            val text = StringBuilder()
            text.append("\n")
            text.append("Installer Version: " + BuildConfig.VERSION_NAME)
            text.append("\n")
            text.append("**********************************")
            text.append("\n")
            for (item in mApps[FAILED_TAB]!!.iterator()) {
                if (mErrorMap.containsKey(item.packageName)) {
                    text.append("App: " + item.title)
                    text.append("\n")
                    text.append("App Package: " + item.packageName)
                    text.append("\n")
                    text.append("App Version: " + item.version)
                    text.append("\n")
                    text.append("Error Log: " + mErrorMap[item.packageName])
                    text.append("\n")
                    text.append("-------------------")
                    text.append("\n")
                }
            }

            emailIntent.putExtra(Intent.EXTRA_TEXT, text.toString())
            startActivity(emailIntent)
            //sheetView.reboot.performClick()
        }

        sheetView.rebootLater.setOnClickListener {
            bottomSheetDialog.dismiss()
            finish()
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var mFragments: ArrayList<AppListFragment> = ArrayList()

        init {
            mFragments.add(AppListFragment.instance(true))
            mFragments[SUCCESS_TAB].setAppList(mApps[SUCCESS_TAB])
            mFragments.add(AppListFragment.instance(true))
            mFragments[FAILED_TAB].setAppList(mApps[FAILED_TAB])
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        fun notifyFragmentDataSetChanged(position: Int) {
            mFragments[position].setAppList(mApps[position])
        }

        override fun getCount(): Int {
            return mFragments.size
        }
    }

    class AppLoader(context: Context, val apps: ArrayList<String>,
                    val errorMap: HashMap<String, String>, private val mCallback: OverlaysActivity.Callback)
        : AsyncTask<Void, AppLoader.Progress, Void>() {

        private val mConRef: WeakReference<Context> = WeakReference(context)

        class Progress(val tab: Int, val item: OverlaysActivity.AppItem)

        override fun doInBackground(vararg params: Void?): Void? {
            assert(mConRef.get() != null)
            val pm = mConRef.get()!!.packageManager
            val context = mConRef.get()
            for (pn: String in apps) {
                var info: ApplicationInfo? = null
                var pInfo: PackageInfo? = null
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                }
                if (info != null) {
                    val item = OverlaysActivity.AppItem()
                    item.packageName = pn
                    item.icon = info.loadIcon(pm)
                    item.title = info.loadLabel(pm) as String
                    item.version = pInfo!!.versionCode
                    if (errorMap.keys.contains(pn)) {
                        onProgressUpdate(Progress(FAILED_TAB, item))
                    } else if (Utils.isOverlayInstalled(context!!, Utils.getOverlayPackageName(pn))) {
                        onProgressUpdate(Progress(SUCCESS_TAB, item))
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
}
