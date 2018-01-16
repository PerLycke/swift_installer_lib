package com.brit.swiftinstaller.ui.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.adapter.AppListAdapter
import kotlinx.android.synthetic.main.app_list_activity.*
import java.util.*
import kotlin.collections.ArrayList

class AppListActivity : AppCompatActivity() {

    private lateinit var adapter: AppListAdapter
    val packages: MutableList<ApplicationInfo> = mutableListOf()
    var apps: ArrayList<ApplicationInfo> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_list_activity)
        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Overlays")

        val appList: ArrayList<ApplicationInfo> = arrayListOf()
        for (pn: String in assets.list("overlays")) {
            Log.d("TEST", "pn - " + pn)
            var info: ApplicationInfo? = null
            try {
                info = packageManager.getApplicationInfo(pn, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {}
            if (info != null) {
                appList.add(info)
            }
        }
        Collections.sort(appList, ApplicationInfo.DisplayNameComparator(packageManager))
        for (i in appList) {
            packages.add(i)
        }

        adapter = AppListAdapter(this, packages)
        appListView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        appListView.layoutManager = layoutManager
        appListView.setHasFixedSize(true)

        /*makeItHappenBtn.setOnClickListener {
            if (activity is MainActivity) run {
                val act: MainActivity = activity as MainActivity
                act.container.currentItem = 3
            }
        }*/
    }
}
