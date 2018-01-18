package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brit.swiftinstaller.R
import kotlinx.android.synthetic.main.app_list_activity.*
import kotlinx.android.synthetic.main.overlay_activity.*
import kotlinx.android.synthetic.main.tab_layout.*

class OverlayActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mApps: HashMap<Int, ArrayList<AppItem>> = HashMap()

    private lateinit var mDialogItems: Array<DialogItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.overlay_activity)

        mDialogItems = arrayOf(
                DialogItem(getDrawable(R.drawable.ic_install), getString(R.string.dialog_install)),
                DialogItem(getDrawable(R.drawable.ic_uninstall), getString(R.string.dialog_uninstall)),
                DialogItem(getDrawable(R.drawable.ic_update), getString(R.string.dialog_update))
        )

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mApps.put(0, ArrayList())
        mApps.put(1, ArrayList())
        mApps.put(2, ArrayList())

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        AppLoader().execute()
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var mFragments: ArrayList<PlaceholderFragment> = ArrayList()

        init {
            mFragments.add(PlaceholderFragment())
            mFragments[0].setAppList(mApps[0]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[1].setAppList(mApps[1]!!)
            mFragments.add(PlaceholderFragment())
            mFragments[2].setAppList(mApps[2]!!)
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

    inner class AppItem {
        var title: String = ""
        var version: Int = 0
        var icon: Drawable? = null
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
                var appName: TextView = view.findViewById(R.id.appItemName)
                var appIcon: ImageView = view.findViewById(R.id.appItemImage)

                fun bindAppItem(item: AppItem) {
                    appName.text = item.title
                    appIcon.setImageDrawable(item.icon)
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class AppLoader: AsyncTask<Void, Void, Void>() {
        var index : Int = 0
        override fun doInBackground(vararg params: Void?): Void? {
            for (pn: String in assets.list("overlays")) {
                Log.d("TEST", "pn - " + pn)
                var info: ApplicationInfo? = null
                var pInfo: PackageInfo? = null
                try {
                    info = packageManager.getApplicationInfo(pn, PackageManager.GET_META_DATA)
                    pInfo = packageManager.getPackageInfo(pn, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                }
                if (info != null) {
                    val item = AppItem()
                    item.icon = info.loadIcon(packageManager)
                    item.title = info.loadLabel(packageManager) as String
                    item.version = pInfo!!.versionCode
                    mApps.get(index)!!.add(item)
                    publishProgress()
                    if (index == 2)
                        index = 0
                    else
                        index++
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            mSectionsPagerAdapter!!.notifyFragmentDataSetChanged()
        }

    }

    fun fabClick(view: View) {
        val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
        builder.setAdapter(DialogAdapter(this, R.layout.install_dialog_item, R.id.dialog_text), { dialog, which ->
            dialog.dismiss()
            when (which) {
                0 -> installAction()
                1 -> uninstallAction()
                2 -> updateAction()
            }
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun installAction() {
        // TODO implement install action
    }

    fun uninstallAction() {
        val uninstallDialog = LayoutInflater.from(this).inflate(R.layout.uninstall_dialog, null)
        val yesBtn = uninstallDialog.findViewById<View>(R.id.yesBtn)
        val cancelBtn = uninstallDialog.findViewById<View>(R.id.cancelBtn)
        val newBuilder = AlertDialog.Builder(this)
                .setView(uninstallDialog)
        val newDialog = newBuilder.show()
        yesBtn.setOnClickListener {
            newDialog.dismiss()
        }
        cancelBtn.setOnClickListener {
            newDialog.dismiss()
        }
    }

    fun updateAction() {
        // TODO implement install action
    }

    /*fun fabClick(view: View) {
        val installDialog = LayoutInflater.from(this).inflate(R.layout.install_dialog, null)
        val uninstallClick = installDialog.findViewById<View>(R.id.uninstallTxt)
        val builder = AlertDialog.Builder(this)
                .setView(installDialog)
        val dialog = builder.show()
        uninstallClick.setOnClickListener {
            dialog.dismiss()
            val uninstallDialog = LayoutInflater.from(this).inflate(R.layout.uninstall_dialog, null)
            val yesBtn = uninstallDialog.findViewById<View>(R.id.yesBtn)
            val cancelBtn = uninstallDialog.findViewById<View>(R.id.cancelBtn)
            val newBuilder = AlertDialog.Builder(this)
                    .setView(uninstallDialog)
            val newDialog = newBuilder.show()
            yesBtn.setOnClickListener {
                newDialog.dismiss()
            }
            cancelBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }*/

    class DialogItem(icon: Drawable, title: String) {
        var dialogIcon = icon
        var dialogTitle = title
    }

    inner class DialogAdapter(context: Context?, resource: Int,
                              textViewResourceId: Int) :
            ArrayAdapter<String>(context, resource, textViewResourceId) {

        val layoutResource = resource

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(this@OverlayActivity).inflate(layoutResource, parent, false)
            }
            var icon: ImageView = view!!.findViewById(R.id.dialog_icon)
            var title: TextView = view.findViewById(R.id.dialog_text)
            icon.setImageDrawable(mDialogItems[position].dialogIcon)
            title.text = mDialogItems[position].dialogTitle
            return view;
        }

        override fun getCount(): Int {
            return mDialogItems.size
        }
    }
}
