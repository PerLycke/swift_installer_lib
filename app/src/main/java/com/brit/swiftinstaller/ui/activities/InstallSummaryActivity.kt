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
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.brit.swiftinstaller.BuildConfig
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getAppVersion
import com.brit.swiftinstaller.utils.setAppVersion
import kotlinx.android.synthetic.main.activity_install_summary.*
import kotlinx.android.synthetic.main.tab_layout_install_summary.*
import java.lang.ref.WeakReference

class InstallSummaryActivity : ThemeActivity() {

    companion object {
        private const val SUCCESS_TAB = 0
        const val FAILED_TAB = 1

        const val ACTION_INSTALL_CANCELLED = "com.brit.swiftinstaller.action.INSTALL_CANCELLED"
    }

    private lateinit var mPagerAdapter: AppsTabPagerAdapter

    private var mErrorMap: HashMap<String, String> = HashMap()
    private lateinit var mApps: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallActivity().finish()
        setContentView(R.layout.activity_install_summary)

        if (intent.extras.containsKey("errorMap")) {
            mErrorMap = Utils.bundleToMap(intent.getBundleExtra("errorMap"))
        }

        if (mErrorMap.isNotEmpty()) {
            send_email_layout.visibility = View.VISIBLE
            send_email_btn.setOnClickListener {
                sendErrorLog()
            }
        }

        mApps = intent.getStringArrayListExtra("apps")

        mPagerAdapter = AppsTabPagerAdapter(supportFragmentManager, true, SUCCESS_TAB, FAILED_TAB)
        mPagerAdapter.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                val dialog = AlertDialog.Builder(this@InstallSummaryActivity, R.style.AppTheme_AlertDialog_Error)

                themeDialog()

                dialog.setTitle(appItem.title)
                dialog.setIcon(appItem.icon)
                dialog.setMessage(mErrorMap[appItem.packageName])
                dialog.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                dialog.show()
            }

        })

        container.adapter = mPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_install_summary_root))
        tab_install_summary_root.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        val builder = AlertDialog.Builder(this)

        themeDialog()

        builder.setTitle(R.string.reboot_to_finish)
        builder.setMessage(R.string.examined_result_msg)
        builder.setPositiveButton(R.string.got_it, { dialogInterface, _ ->
            dialogInterface.dismiss()
        })

        val dialog = builder.create()
        dialog.show()
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

    override fun onResume() {
        super.onResume()
        mApps = intent.getStringArrayListExtra("apps")
        if (intent.extras.containsKey("errorMap")) {
            mErrorMap = Utils.bundleToMap(intent.getBundleExtra("errorMap"))
        } else {
            mErrorMap.clear()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mApps = intent.getStringArrayListExtra("apps")
        if (intent.extras.containsKey("errorMap")) {
            mErrorMap = Utils.bundleToMap(intent.getBundleExtra("errorMap"))
        } else {
            mErrorMap.clear()
        }
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
                text.append("App Version: " + item.versionName)
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
        private val mHandler = Handler()

        class Progress(val tab: Int, val item: AppItem)

        override fun doInBackground(vararg params: Void?): Void? {
            assert(mConRef.get() != null)
            val pm = mConRef.get()!!.packageManager
            val context = mConRef.get()
            apps.addAll(errorMap.keys)
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
                    item.versionCode = pInfo!!.versionCode
                    item.versionName = pInfo.versionName
                    if (errorMap.keys.contains(pn)) {
                        onProgressUpdate(Progress(FAILED_TAB, item))
                    } else if (Utils.isOverlayInstalled(context!!, Utils.getOverlayPackageName(pn))
                            && oInfo!!.versionCode > getAppVersion(context, pn)) {
                        setAppVersion(context, pn, oInfo.versionCode)
                        onProgressUpdate(Progress(SUCCESS_TAB, item))
                    } else {
                        errorMap[pn] = "Install Cancelled"
                        LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(Intent(ACTION_INSTALL_CANCELLED))
                        onProgressUpdate(Progress(FAILED_TAB, item))
                    }
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg progress: Progress?) {
            super.onProgressUpdate(*progress)
            mHandler.post {
                mCallback.updateApps(progress[0]!!.tab, progress[0]!!.item)
            }
        }

    }
}
