package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.view.View
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.ThemedBottomSheetDialog
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getAppVersion
import com.brit.swiftinstaller.utils.setAppVersion
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_install_summary.*
import kotlinx.android.synthetic.main.sheet_install_summary_fab.view.*
import kotlinx.android.synthetic.main.tab_layout_install_summary.*
import java.lang.ref.WeakReference

class InstallSummaryActivity : AppCompatActivity() {

    companion object {
        private const val SUCCESS_TAB = 0
        const val FAILED_TAB = 1
    }

    private lateinit var mPagerAdapter: AppsTabPagerAdapter

    private var mErrorMap: HashMap<String, String> = HashMap()
    private lateinit var mApps: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_summary)

        if (intent.extras.containsKey("errorMap")) {
            mErrorMap = Utils.bundleToMap(intent.getBundleExtra("errorMap"))
        }

        if (mErrorMap.isNotEmpty()) {
            sendEmailLayout.visibility = View.VISIBLE
            sendEmailBtn.setOnClickListener {
                sendErrorLog()
            }
        }

        mApps = intent.getStringArrayListExtra("apps")

        mPagerAdapter = AppsTabPagerAdapter(supportFragmentManager, true, SUCCESS_TAB, FAILED_TAB)
        mPagerAdapter.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                val dialog = AlertDialog.Builder(this@InstallSummaryActivity,
                        Utils.getDialogTheme(this@InstallSummaryActivity))
                        .setTitle(appItem.title)
                        .setIcon(appItem.icon)
                        .setMessage(mErrorMap[appItem.packageName])
                        .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                dialog.show()
            }

        })

        container.adapter = mPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_install", true)) {
            val builder = AlertDialog.Builder(this)
                    .setTitle(R.string.reboot_to_finish)
                    .setMessage(R.string.examined_result_msg)
                    .setPositiveButton(R.string.got_it, { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })

            val dialog = builder.create()
            dialog.show()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_install", false).apply()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mPagerAdapter.clearApps()
        AppLoader(this, mApps, mErrorMap, object : OverlaysActivity.Callback {
            override fun updateApps(tab: Int, item: AppItem) {
                mPagerAdapter.addApp(tab, item)
            }
        }).execute()
    }

    @Suppress("UNUSED_PARAMETER")
    fun rebootActions(view: View) {
        val bottomSheetDialog = ThemedBottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_install_summary_fab, null)

        if (mErrorMap.isEmpty()) {
            sheetView.sendLog.visibility = View.GONE
        } else {
            sheetView.sendLog.visibility = View.VISIBLE
        }

        sheetView.reboot.setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            bottomSheetDialog.dismiss()
            pm.reboot(null)
        }

        sheetView.sendLog.setOnClickListener {
            sendErrorLog()
        }

        sheetView.rebootLater.setOnClickListener {
            bottomSheetDialog.dismiss()
            finish()
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    private fun sendErrorLog() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, Array(1, { "swiftuserhelp@gmail.com" }))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Swift Installer: Error Log")

        val text = StringBuilder()
        text.append("\n")
        text.append("Installer Version: ${BuildConfig.VERSION_NAME}")
        text.append("\n")
        text.append("Device: ${Build.DEVICE}")
        text.append("\n")
        text.append("Android Version: ${Build.VERSION.RELEASE}")
        text.append("\n")
        text.append("**********************************")
        text.append("\n")
        for (item in mPagerAdapter.getApps(FAILED_TAB).iterator()) {
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
    }

    class AppLoader(context: Context, val apps: ArrayList<String>,
                    private val errorMap: HashMap<String, String>,
                    private val mCallback: OverlaysActivity.Callback) :
            AsyncTask<Void, AppLoader.Progress, Void>() {

        private val mConRef: WeakReference<Context> = WeakReference(context)

        class Progress(val tab: Int, val item: AppItem)

        override fun doInBackground(vararg params: Void?): Void? {
            assert(mConRef.get() != null)
            val pm = mConRef.get()!!.packageManager
            val context = mConRef.get()
            for (pn: String in apps) {
                var info: ApplicationInfo? = null
                var pInfo: PackageInfo? = null
                var oInfo: PackageInfo? = null
                try {
                    info = pm.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = pm.getPackageInfo(pn, 0)
                    oInfo = pm.getPackageInfo(Utils.getOverlayPackageName(pn), 0)
                } catch (e: PackageManager.NameNotFoundException) {
                }
                if (info != null) {
                    val item = AppItem()
                    item.packageName = pn
                    item.icon = info.loadIcon(pm)
                    item.title = info.loadLabel(pm) as String
                    item.version = pInfo!!.versionCode
                    if (errorMap.keys.contains(pn)) {
                        onProgressUpdate(Progress(FAILED_TAB, item))
                    } else if (Utils.isOverlayInstalled(context!!, Utils.getOverlayPackageName(pn))
                            && oInfo!!.versionCode > getAppVersion(context, pn)) {
                        setAppVersion(context, pn, oInfo.versionCode)
                        onProgressUpdate(Progress(SUCCESS_TAB, item))
                    } else {
                        errorMap[pn] = "Install Cancelled"
                        onProgressUpdate(Progress(FAILED_TAB, item))
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
