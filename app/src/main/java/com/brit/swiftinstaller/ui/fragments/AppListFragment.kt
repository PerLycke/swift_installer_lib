package com.brit.swiftinstaller.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.activities.OverlaysActivity
import kotlinx.android.synthetic.main.activity_app_list.*

class AppListFragment : Fragment() {

    var mApps: ArrayList<OverlaysActivity.AppItem> = ArrayList()

    var mSummary = false

    companion object {
        fun instance(summary: Boolean): AppListFragment {
            val fragment = AppListFragment()
            val args = Bundle()
            args.putBoolean("summary", summary)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (arguments != null)
            mSummary = arguments!!.getBoolean("summary", false)

        appListView.adapter = AppAdapter()
        appListView.layoutManager = LinearLayoutManager(activity)
    }

    fun setAppList(apps: ArrayList<OverlaysActivity.AppItem>?) {
        mApps.clear()
        mApps.addAll(apps!!)
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

            fun bindAppItem(item: OverlaysActivity.AppItem) {
                appName.text = item.title
                appIcon.setImageDrawable(item.icon)
                appCheckBox.isChecked = item.checked
//                    packageName.text = item.packageName

                if (mSummary) {
                    appCheckBox.visibility = View.GONE
                }

                appCheckBox.setOnCheckedChangeListener({ _: CompoundButton, checked: Boolean ->
                    item.checked = checked
                })
            }
        }

    }
}