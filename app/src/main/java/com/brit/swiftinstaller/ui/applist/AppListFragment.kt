package com.brit.swiftinstaller.ui.applist

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.brit.swiftinstaller.utils.getHideFailedInfoCard
import com.brit.swiftinstaller.utils.setHideFailedInfoCard
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.activity_app_list.view.*
import kotlinx.android.synthetic.main.failed_info_card.view.*

class AppListFragment : Fragment() {

    var mApps: ArrayList<AppItem> = ArrayList()
    private val mHandler = Handler()

    private val mChecked = SparseBooleanArray()

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
        view.failed_info.closeInfoCard.setOnClickListener {
            view.failed_info.visibility = View.GONE
            setHideFailedInfoCard(context!!, true)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appListView.adapter = AppAdapter()
        appListView.layoutManager = LinearLayoutManager(activity)
    }

    fun getCheckedItems(): ArrayList<AppItem> {
        val apps = ArrayList<AppItem>()
        for (i in 0.until(mChecked.size())) {
            Log.d("TEST", "i - $i")
            Log.d("TEST", "key - ${mChecked.keyAt(i)}")
            if (mChecked.get(mChecked.keyAt(i), false)) {
                Log.d("TEST", "checked - ${mApps[mChecked.keyAt(i)].packageName}")
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
        mApps.addAll(apps!!)
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
            holder.bindAppItem(mApps[position])
        }

        override fun getItemCount(): Int {
            return mApps.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private var appName: TextView = view.findViewById(R.id.appItemName)
            //                var packageName: TextView = view.findViewById(R.id.appName)
            private var appIcon: ImageView = view.findViewById(R.id.appItemImage)
            private var appCheckBox: CheckBox = view.findViewById(R.id.appItemCheckBox)

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
                    appCheckBox.visibility = View.GONE
                }
            }
        }

    }
}