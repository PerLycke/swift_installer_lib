package com.brit.swiftinstaller.ui.applist

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.brit.swiftinstaller.utils.getAppsToUpdate
import com.brit.swiftinstaller.utils.getHideFailedInfoCard
import com.brit.swiftinstaller.utils.setHideFailedInfoCard
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.activity_app_list.view.*
import kotlinx.android.synthetic.main.failed_info_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class AppListFragment : androidx.fragment.app.Fragment() {

    var mApps: ArrayList<AppItem> = ArrayList()
    var mVisible: ArrayList<Int> = ArrayList()
    private val mHandler = Handler()

    var requiredApps: Array<String> = emptyArray()

    private val mChecked = SparseBooleanArray()

    var alertIconClickListener: AlertIconClickListener? = null
    var appListView: RecyclerView? = null

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
        view.failed_info.failed_info_card_close.setOnClickListener {
            view.failed_info.visibility = View.GONE
            setHideFailedInfoCard(context!!, true)
        }
        appListView = view.app_list_view
        return view
    }

    fun querySearch(query: String) {
        mVisible.clear()
        if (query.isEmpty() || query.isBlank()) {
            mApps.forEach { mVisible.add(mApps.indexOf(it)) }
        } else {
            mApps.forEach {
                if (it.title.toLowerCase().contains(query.toLowerCase())) {
                    mVisible.add(mApps.indexOf(it))
                }
            }
        }
        if (appListView != null && !appListView!!.isComputingLayout) {
            appListView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appListView?.adapter = AppAdapter()
        appListView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
    }

    fun getCheckedItems(): ArrayList<AppItem> {
        val apps = ArrayList<AppItem>()
        for (i in mApps.indices) {
            if (mChecked.get(i) || requiredApps.contains(mApps[i].packageName)) {
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
        mVisible.addAll(mApps.indices)
        mHandler.post {
            if (appListView != null && !appListView!!.isComputingLayout) {
                appListView?.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun setRequiredAppList(apps: Array<String>) {
        requiredApps = apps
    }

    inner class AppAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<AppAdapter.ViewHolder>() {

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

        inner class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            private var appName: TextView = view.findViewById(R.id.app_item_name)
            private var packageName: TextView = view.findViewById(R.id.app_name)
            private var appIcon: ImageView = view.findViewById(R.id.app_item_image)
            private var appCheckBox: CheckBox = view.findViewById(R.id.app_item_checkbox)
            private var alertIcon: ImageView = view.findViewById(R.id.alert_icon)
            private var required: TextView = view.findViewById(R.id.required)

            private val checkListener: (CompoundButton, Boolean) -> Unit
            private val clickListener: (View) -> Unit

            init {
                clickListener = {
                    appCheckBox.toggle()
                }

                checkListener = { _: CompoundButton, checked: Boolean ->
                    mChecked.put(mVisible[adapterPosition], checked)
                }
            }

            fun bindAppItem(item: AppItem) {
                appName.text = item.title
                appIcon.setImageDrawable(item.icon)
                if (requiredApps.contains(item.packageName)) {
                    appCheckBox.setOnCheckedChangeListener(null)
                    view.setOnClickListener(null)
                    appCheckBox.isChecked = true
                    appCheckBox.isClickable = false
                    view.isEnabled = false
                    required.visibility = View.VISIBLE
                    appCheckBox.buttonTintList = ColorStateList(arrayOf<IntArray>(appCheckBox.drawableState),
                            intArrayOf(Color.parseColor("#4dffffff")))

                    //val states = arrayOf<IntArray>(intArrayOf(android.R.attr.state_checked))
                    //val colors = intArrayOf(Color.parseColor("#4dffffff"))
                    //appCheckBox.setButtonTintList(ColorStateList(states, colors))
                } else {
                    appCheckBox.setOnCheckedChangeListener(checkListener)
                    appCheckBox.isChecked = mChecked.get(mVisible[adapterPosition], false)
                    view.setOnClickListener(clickListener)
                    view.isEnabled = true
                    required.visibility = View.GONE
                    appCheckBox.buttonTintList = null
                }
                packageName.text = item.packageName

                appCheckBox.setOnCheckedChangeListener(checkListener)

                if (mSummary) {
                    appCheckBox.visibility = View.INVISIBLE
                    appCheckBox.isEnabled = false
                    if (mFailedTab) {
                        alertIcon.visibility = View.VISIBLE
                    }
                } else {
                    if (!Utils.checkVersionCompatible(context!!, item.packageName)) {
                        appName.alpha = 0.3f
                        if (Utils.isOverlayInstalled(context!!, Utils.getOverlayPackageName(item.packageName))) {
                            appCheckBox.visibility = View.VISIBLE
                            appCheckBox.isEnabled = true
                            required.visibility = View.VISIBLE
                            required.text = "Unsupported, please uninstall"
                        } else {
                            appCheckBox.visibility = View.INVISIBLE
                            appCheckBox.isEnabled = false
                        }
                    } else {
                        appName.alpha = 1.0f
                        appCheckBox.visibility = View.VISIBLE
                    }
                    if (Utils.isOverlayInstalled(context!!, item.packageName)
                            && getAppsToUpdate(context!!).contains(item.packageName)) {
                        appName.setTextColor(context!!.getColor(R.color.minimal_orange))
                    } else if (requiredApps.contains(item.packageName)) {
                        appName.setTextColor(Color.parseColor("#4dffffff"))
                    } else {
                        appName.setTextColor(context!!.getColor(android.R.color.white))
                    }
                    if (Utils.overlayHasVersion(context!!, item.packageName)) {
                        alertIcon.visibility = View.VISIBLE
                    } else {
                        alertIcon.visibility = View.GONE
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