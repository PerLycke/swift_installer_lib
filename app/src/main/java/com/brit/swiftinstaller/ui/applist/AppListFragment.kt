package com.brit.swiftinstaller.ui.applist

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.text.TextUtilsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.Utils
import com.brit.swiftinstaller.utils.getHideFailedInfoCard
import com.brit.swiftinstaller.utils.setHideFailedInfoCard
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.activity_app_list.view.*
import kotlinx.android.synthetic.main.failed_info_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class AppListFragment : Fragment() {

    var mApps: ArrayList<AppItem> = ArrayList()
    var mVisible: ArrayList<Int> = ArrayList()
    private val mHandler = Handler()

    private val mChecked = SparseBooleanArray()

    var alertIconClickListener: AlertIconClickListener? = null

    private var mSummary = false
    private var mFailedTab = false

    companion object {
        fun instance(summary: Boolean, failedTab: Boolean): AppListFragment {
            val fragment = AppListFragment()
            val args = Bundle()
            args.putBoolean("summary", summary)
            args.putBoolean("failed_tab", failedTab)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_app_list, container, false)
        if (arguments != null) {
            mSummary = arguments!!.getBoolean("summary", false)
            mFailedTab = arguments!!.getBoolean("failed_tab", false)
        }
        if (mSummary && mFailedTab && !getHideFailedInfoCard(context!!)) {
            view.failed_info.visibility = View.VISIBLE
        } else {
            view.failed_info.visibility = View.GONE
        }
        selectAll(false)
        view.failed_info.closeInfoCard.setOnClickListener {
            view.failed_info.visibility = View.GONE
            setHideFailedInfoCard(context!!, true)
        }
        return view
    }

    fun querySearch(query: String) {
        mVisible.clear()
        if (query.isEmpty() || query.isBlank()) {
            mApps.forEach { mVisible.add(mApps.indexOf(it)) }
        } else {
            mApps.forEach {
                if (it.packageName.toLowerCase().contains(query.toLowerCase())
                        || it.title.toLowerCase().contains(query.toLowerCase())) {
                    mVisible.add(mApps.indexOf(it))
                }
            }
        }
        appListView.adapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appListView.adapter = AppAdapter()
        appListView.layoutManager = LinearLayoutManager(activity)
    }

    fun getCheckedItems(): ArrayList<AppItem> {
        val apps = ArrayList<AppItem>()
        for (i in mApps.indices) {
            Log.d("TEST", "i - $i")
            if (mChecked.get(i)) {
                Log.d("TEST", "$i : checked - ${mApps[i].packageName}")
                apps.add(mApps[i])
            }
        }
        return apps
    }

    fun selectAll(checked: Boolean) {
        for (index in mApps.indices) {
            mChecked.put(index, checked)
        }
    }

    fun setAppList(apps: ArrayList<AppItem>?) {
        mApps.clear()
        mVisible.clear()
        mApps.addAll(apps!!)
        mApps.sortWith(Comparator { o1: AppItem, o2: AppItem ->
            o1.title.compareTo(o2.title)
        })
        mApps.forEach { mVisible.add(mApps.indexOf(it)) }
        if (appListView != null && !appListView.isComputingLayout) {
            mHandler.post {
                appListView.adapter.notifyDataSetChanged()
            }
        }
    }

    inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(
                    R.layout.app_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindAppItem(mApps[mVisible[position]])
        }

        override fun getItemCount(): Int {
            return mVisible.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private var appName: TextView = view.findViewById(R.id.appItemName)
            //                var packageName: TextView = view.findViewById(R.id.appName)
            private var appIcon: ImageView = view.findViewById(R.id.appItemImage)
            private var appCheckBox: CheckBox = view.findViewById(R.id.appItemCheckBox)
            private var alertIcon: ImageView = view.findViewById(R.id.alertIcon)

            init {
                view.setOnClickListener {
                    appCheckBox.toggle()
                }

                appCheckBox.setOnCheckedChangeListener({ _: CompoundButton, checked: Boolean ->
                    mChecked.put(adapterPosition, checked)
                    Log.d("TEST", "checked - $mChecked")
                })
            }

            fun bindAppItem(item: AppItem) {
                appName.text = item.title
                appIcon.setImageDrawable(item.icon)
                appCheckBox.isChecked = mChecked.get(adapterPosition, false)
//                    packageName.text = item.packageName

                if (mSummary) {
                    appCheckBox.visibility = View.INVISIBLE
                    appCheckBox.isEnabled = false
                    if (mFailedTab) {
                        alertIcon.visibility = View.VISIBLE
                    }
                } else {
                    if (!Utils.checkVersionCompatible(context!!, item.packageName)) {
                        appName.alpha = 0.3f
                        alertIcon.visibility = View.VISIBLE
                        appCheckBox.visibility = View.INVISIBLE
                        appCheckBox.isEnabled = false
                    } else {
                        appName.alpha = 1.0f
                        alertIcon.visibility = View.GONE
                        appCheckBox.visibility = View.VISIBLE
                    }
                }

                if (alertIconClickListener != null) {
                    alertIcon.setOnClickListener {
                        alertIconClickListener!!.onAlertIconClick(mApps[adapterPosition])
                    }
                }
            }
        }

    }

    interface AlertIconClickListener {
        fun onAlertIconClick(appItem: AppItem)
    }
}