package com.brit.swiftinstaller.ui.applist

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.utils.*
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.app_item.view.*
import kotlinx.android.synthetic.main.app_option_item.view.*
import kotlinx.android.synthetic.main.failed_info_card.view.*

class AppListFragment : Fragment() {

    var mApps: ArrayList<AppItem> = ArrayList()
    var mVisible: ArrayList<Int> = ArrayList()

    var requiredApps: Array<String> = emptyArray()

    private val mChecked = SparseBooleanArray()

    var alertIconClickListener: AlertIconClickListener? = null
    var appCheckBoxClickListener: AppCheckBoxClickListener? = null
    var viewClickListener: ViewClickListener? = null

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
            view.failed_info_card.visibility = View.VISIBLE
        } else {
            view.failed_info_card.visibility = View.GONE
        }
        selectAll(false)
        view.failed_info_card.failed_info_card_close.setOnClickListener {
            view.failed_info_card.visibility = View.GONE
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
                if (it.title.toLowerCase().contains(query.toLowerCase())) {
                    mVisible.add(mApps.indexOf(it))
                }
            }
        }
        if (app_list_view != null && !app_list_view.isComputingLayout) {
            app_list_view.adapter?.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app_list_view.adapter = AppAdapter()
        app_list_view.layoutManager = LinearLayoutManager(activity)
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
        mVisible.addAll(mApps.indices)
        if (app_list_view != null && !app_list_view.isComputingLayout) {
            app_list_view.adapter?.notifyDataSetChanged()
        }
    }

    fun setRequiredAppList(apps: Array<String>) {
        requiredApps = apps
    }

    inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(
                    R.layout.app_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindAppItem(mApps[mVisible[position]])

            if (position + 1 == itemCount) {
                setBottomMargin(holder.itemView, (64 * Resources.getSystem().displayMetrics.density).toInt())
            } else {
                setBottomMargin(holder.itemView, 0)
            }
        }

        private fun setBottomMargin(view: View, bottomMargin: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
                view.requestLayout()
            }
        }

        override fun getItemCount(): Int {
            return mVisible.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private var appName: TextView = view.findViewById(R.id.app_item_name)
            private var packageName: TextView = view.findViewById(R.id.app_name)
            private var appIcon: ImageView = view.findViewById(R.id.app_item_image)
            private var appCheckBox: CheckBox = view.findViewById(R.id.app_item_checkbox)
            private var alertIcon: ImageView = view.findViewById(R.id.alert_icon)
            private var downloadIcon: ImageView = view.findViewById(R.id.download_icon)
            private var required: TextView = view.findViewById(R.id.required)
            private var blockedPackagesAlert: ImageView = view.findViewById(R.id.blocked_packages_alert)

            private val checkListener: (CompoundButton, Boolean) -> Unit
            private val clickListener: (View) -> Unit

            init {
                clickListener = {
                    viewClickListener!!.onClick(mApps[mVisible[adapterPosition]])
                    appCheckBox.toggle()
                }
                checkListener = { _: CompoundButton, checked: Boolean ->
                    mChecked.put(mVisible[adapterPosition], checked)
                }
            }

            fun bindAppItem(item: AppItem) {
                val incompatible = !Utils.checkVersionCompatible(context!!, item.packageName)
                val installed = RomInfo.getRomInfo(context!!).isOverlayInstalled(item.packageName)
                val hasVersions = Utils.overlayHasVersion(context!!, item.packageName)
                val hasUpdate = getAppsToUpdate(context!!).contains(item.packageName)
                val isRequired = requiredApps.contains(item.packageName)
                appName.text = item.title
                appName.setTextColor(context!!.getColor(android.R.color.white))
                appName.alpha = 1.0f
                appIcon.setImageDrawable(item.icon)
                packageName.text = item.packageName
                view.setOnClickListener(clickListener)
                appCheckBox.visibility = View.VISIBLE
                appCheckBox.setOnCheckedChangeListener(checkListener)
                appCheckBox.setOnClickListener {
                    appCheckBoxClickListener!!.onCheckBoxClick(mApps[mVisible[adapterPosition]])
                }
                appCheckBox.isChecked = mChecked.get(mVisible[adapterPosition], false)
                appCheckBox.alpha = 1.0f
                alertIcon.visibility = View.GONE
                alertIcon.setImageDrawable(context!!.getDrawable(R.drawable.ic_info))
                required.visibility = View.GONE
                downloadIcon.visibility = View.GONE
                blockedPackagesAlert.visibility = View.GONE

                val appOptions = OverlayUtils.getOverlayOptions(context!!, item.packageName)
                if (appOptions.isNotEmpty()) {
                    view.options_icon.visibility = View.VISIBLE
                    view.options_icon.setOnClickListener {
                        val dialog = AlertDialog.Builder(context!!)
                        dialog.setTitle(item.title)
                        dialog.setIcon(item.icon)
                        dialog.setAdapter(OptionsAdapter(context!!, appOptions)) { _, _ ->
                        }
                        dialog.show()
                    }
                } else {
                    view.options_icon.visibility = View.GONE
                }

                if (mSummary) {
                    appCheckBox.visibility = View.GONE
                    appCheckBox.isEnabled = false
                    view.isClickable = false
                    if (mFailedTab) {
                        alertIcon.visibility = View.VISIBLE
                        alertIcon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                    }
                } else {
                    if (isRequired) {
                        appCheckBox.isChecked = true
                        appCheckBox.isClickable = false
                        appCheckBox.alpha = 0.3f
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.required)
                        appName.setTextColor(Color.parseColor("#4dffffff"))
                        view.isClickable = false
                    }
                    if (incompatible) {
                        appName.alpha = 0.3f
                        alertIcon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.unsupported)
                        if (!installed) {
                            appCheckBox.visibility = View.GONE
                            appCheckBox.isClickable = false
                            appCheckBox.isChecked = false
                            view.isClickable = false
                            required.visibility = View.GONE
                        }
                    }
                    if (hasUpdate) {
                        appName.setTextColor(context!!.getColor(R.color.minimal_orange))
                    }
                    if (hasVersions) {
                        alertIcon.visibility = View.VISIBLE
                    }
                    if (appName.text.contains("Gboard")) {
                        downloadIcon.visibility = View.VISIBLE
                        downloadIcon.setColorFilter(getAccentColor(context!!))
                    }
                    if (appName.text.contains("Samsung Music") || appName.text.contains("Voice Recorder")) {
                        blockedPackagesAlert.visibility = View.VISIBLE
                        blockedPackagesAlert.setColorFilter(getAccentColor(context!!))
                    }
                }

                if (alertIcon.visibility == View.VISIBLE) {
                    alertIcon.setOnClickListener {
                        alertIconClickListener!!.onAlertIconClick(mApps[mVisible[adapterPosition]])
                    }
                }
            }
        }

    }

    class OptionsAdapter(context: Context, val options: ArrayMap<String, Array<String>>) : ArrayAdapter<String>(context, R.layout.app_option_item) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.app_option_item, parent, false)

            view.options_title.text = options.keyAt(position)
            val opts = options[options.keyAt(position)]
            if (opts!!.contains("on")) {
                view.checkbox.visibility = View.VISIBLE
                view.spinner.visibility = View.GONE
            } else {
                view.spinner.visibility = View.VISIBLE
                view.checkbox.visibility = View.GONE
                view.spinner.adapter = ArrayAdapter<String>(context, R.layout.spinner_item, opts)
            }
            return view
        }

        override fun getCount(): Int {
            return options.size
        }
    }

    interface AlertIconClickListener {
        fun onAlertIconClick(appItem: AppItem)
    }

    interface AppCheckBoxClickListener {
        fun onCheckBoxClick(appItem: AppItem)
    }

    interface ViewClickListener {
        fun onClick(appItem: AppItem)
    }
}