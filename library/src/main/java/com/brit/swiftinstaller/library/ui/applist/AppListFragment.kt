/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library.ui.applist

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.InstallActivity
import com.brit.swiftinstaller.library.utils.AppExtrasHandler
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.OverlayUtils
import com.brit.swiftinstaller.library.utils.OverlayUtils.checkVersionCompatible
import com.brit.swiftinstaller.library.utils.OverlayUtils.overlayHasVersion
import com.brit.swiftinstaller.library.utils.SynchronizedArrayList
import com.brit.swiftinstaller.library.utils.alert
import com.brit.swiftinstaller.library.utils.getAppsToUpdate
import com.brit.swiftinstaller.library.utils.getHiddenApps
import com.brit.swiftinstaller.library.utils.getHideFailedInfoCard
import com.brit.swiftinstaller.library.utils.getSelectedOverlayOptions
import com.brit.swiftinstaller.library.utils.setHideFailedInfoCard
import com.brit.swiftinstaller.library.utils.setOverlayOption
import com.brit.swiftinstaller.library.utils.setVisible
import com.brit.swiftinstaller.library.utils.swift
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.app_item.*
import kotlinx.android.synthetic.main.app_option_item.view.*
import kotlinx.android.synthetic.main.failed_info_card.view.*

class AppListFragment : Fragment() {

    var mApps: SynchronizedArrayList<AppItem> = SynchronizedArrayList()
    var mVisible: SynchronizedArrayList<Int> = SynchronizedArrayList()

    var requiredApps: Array<String> = emptyArray()

    lateinit var appExtrasHandler: AppExtrasHandler

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        appExtrasHandler = context!!.swift.extrasHandler
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

    fun getCheckedItems(): SynchronizedArrayList<AppItem> {
        val apps = SynchronizedArrayList<AppItem>()
        for (i in mApps.indices) {
            if (mChecked.get(i) || requiredApps.contains(mApps[i].packageName)) {
                apps.add(mApps[i])
            }
        }
        return apps
    }

    fun selectAll(checked: Boolean) {
        for (index in mApps.indices) {
            if (context != null) {
                if (checkVersionCompatible(context!!, mApps[index].packageName) ||
                        context!!.swift.romInfo.isOverlayInstalled(mApps[index].packageName)) {
                    mChecked.put(index, checked)
                }
            } else {
                mChecked.put(index, checked)
            }
        }
    }

    fun setAppList(apps: SynchronizedArrayList<AppItem>?) {
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
                setBottomMargin(holder.itemView,
                        (64 * Resources.getSystem().displayMetrics.density).toInt())
            } else {
                setBottomMargin(holder.itemView, 0)
            }
        }

        private fun setBottomMargin(view: View, bottomMargin: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                        bottomMargin)
                view.requestLayout()
            }
        }

        override fun getItemCount(): Int {
            return mVisible.size
        }

        inner class ViewHolder(override val containerView: View) :
                RecyclerView.ViewHolder(containerView), LayoutContainer {

            private val checkListener: (CompoundButton, Boolean) -> Unit
            private val clickListener: (View) -> Unit = {
                viewClickListener!!.onClick(mApps[mVisible[adapterPosition]])
                app_item_checkbox.toggle()
            }

            init {
                checkListener = { _: CompoundButton, checked: Boolean ->
                    mChecked.put(mVisible[adapterPosition], checked)
                }
            }

            fun bindAppItem(item: AppItem) {
                val incompatible = !checkVersionCompatible(context!!, item.packageName)
                val installed = context!!.swift.romInfo.isOverlayInstalled(item.packageName)
                val hasVersions = overlayHasVersion(context!!, item.packageName)
                val hasUpdate = getAppsToUpdate(context!!).contains(item.packageName)
                val isRequired = requiredApps.contains(item.packageName)
                app_item_name.text = item.title
                app_item_name.setTextColor(context!!.getColor(android.R.color.white))
                app_item_name.alpha = 1.0f
                app_item_image.setImageDrawable(item.icon)
                app_name.text = item.packageName
                containerView.setOnClickListener(clickListener)
                app_item_checkbox.visibility = View.VISIBLE
                app_item_checkbox.setOnCheckedChangeListener(checkListener)
                app_item_checkbox.setOnClickListener {
                    appCheckBoxClickListener!!.onCheckBoxClick(mApps[mVisible[adapterPosition]])
                }
                app_item_checkbox.isChecked = mChecked.get(mVisible[adapterPosition], false)
                app_item_checkbox.alpha = 1.0f
                alert_icon.visibility = View.GONE
                alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_info))
                required.visibility = View.GONE
                download_icon.visibility = View.GONE
                blocked_packages_alert.visibility = View.GONE

                val appOptions = OverlayUtils.getOverlayOptions(context!!, item.packageName)
                if (appOptions.isNotEmpty() && !mSummary) {
                    val optionsSelection = SynchronizedArrayList<String>()
                    val selected = getSelectedOverlayOptions(context!!, item.packageName)
                    for (i in appOptions.keys.indices) {
                        if (selected.containsKey(appOptions.keyAt(i))) {
                            optionsSelection.add(i, selected[appOptions.keyAt(i)] ?: "")
                        }
                    }
                    options_icon.visibility = View.VISIBLE
                    options_icon.setColorFilter(
                            activity!!.swift.romInfo.getCustomizeHandler().getSelection().accentColor)

                    options_icon.setOnClickListener {
                        context!!.alert {
                            title = item.title
                            icon = item.icon
                            adapter(OptionsAdapter(ctx, appOptions, optionsSelection)) { _, _ ->
                            }
                            positiveButton("Apply") { dialog ->
                                for (pos in appOptions.keys.indices) {
                                    val value = optionsSelection.elementAtOrNull(pos)
                                    if (value != null) {
                                        setOverlayOption(context!!, item.packageName,
                                                appOptions.keyAt(pos), value)
                                    }
                                }
                                if (installed) {
                                    val intent = Intent(context!!, InstallActivity::class.java)
                                    val apps = SynchronizedArrayList<String>()
                                    apps.add(item.packageName)
                                    intent.putStringArrayListExtra("apps", apps)
                                    startActivity(intent)
                                }
                                dialog.dismiss()
                            }
                            negativeButton(R.string.cancel) { dialog ->
                                dialog.dismiss()
                            }
                            show()
                        }
                    }
                } else {
                    options_icon.visibility = View.GONE
                }

                if (mSummary) {
                    app_item_checkbox.visibility = View.GONE
                    app_item_checkbox.isEnabled = false
                    containerView.isClickable = false
                    if (mFailedTab) {
                        alert_icon.visibility = View.VISIBLE
                        alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                    }
                } else {
                    if (isRequired) {
                        app_item_checkbox.isChecked = true
                        app_item_checkbox.isClickable = false
                        app_item_checkbox.alpha = 0.3f
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.required)
                        app_item_name.setTextColor(Color.parseColor("#4dffffff"))
                        containerView.isClickable = false
                    }
                    if (incompatible) {
                        app_item_name.alpha = 0.3f
                        alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.unsupported)
                        if (!installed) {
                            app_item_checkbox.visibility = View.GONE
                            app_item_checkbox.isClickable = false
                            app_item_checkbox.isChecked = false
                            containerView.isClickable = false
                            required.visibility = View.GONE
                        }
                    }
                    if (hasUpdate && installed) {
                        app_item_name.setTextColor(context!!.getColor(R.color.minimal_orange))
                    }
                    if (hasVersions) {
                        alert_icon.visibility = View.VISIBLE
                    }
                    if (appExtrasHandler.appExtras.containsKey(item.packageName)) {
                        download_icon.visibility = View.VISIBLE
                        download_icon.setColorFilter(context!!.swift.selection.accentColor)
                        download_icon.setOnClickListener {
                            appExtrasHandler.appExtras[item.packageName]?.invoke(
                                    activity!! as AppCompatActivity)
                        }
                    }
                    if (app_item_name.text.contains("Samsung Music") || app_item_name.text.contains(
                                    "Voice Recorder")) {
                        blocked_packages_alert.visibility = View.VISIBLE
                        blocked_packages_alert.setColorFilter(context!!.swift.selection.accentColor)
                    }
                }

                if (alert_icon.visibility == View.VISIBLE) {
                    alert_icon.setOnClickListener {
                        alertIconClickListener!!.onAlertIconClick(mApps[mVisible[adapterPosition]])
                    }
                }

                if (context!!.swift.extrasHandler.appExtras.containsKey(item.packageName)
                        && getHiddenApps(context!!).contains(item.packageName)) {
                    app_item_checkbox.isClickable = false
                    app_item_checkbox.setVisible(false)
                }
            }
        }

    }

    class OptionsAdapter(context: Context, val options: ArrayMap<String, Array<String>>,
                         val selection: SynchronizedArrayList<String>) :
            ArrayAdapter<String>(context, R.layout.app_option_item) {

        private val mHandler = Handler()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.app_option_item,
                    parent, false)

            view.options_title.text = options.keyAt(position)
            val opts = options[options.keyAt(position)]
            if (opts!!.contains("on")) {
                view.checkbox.visibility = View.VISIBLE
                view.checkbox.setOnCheckedChangeListener { _, b ->
                    if (selection.elementAtOrNull(position) != null)
                        selection.removeAt(position)
                    mHandler.post {
                        selection.add(position, if (b) {
                            "on"
                        } else {
                            "off"
                        })
                    }
                }
                view.checkbox.isChecked = (selection.elementAtOrNull(position) ?: "off") == "on"
                view.spinner.visibility = View.GONE
            } else {
                view.spinner.visibility = View.VISIBLE
                view.checkbox.visibility = View.GONE
                view.spinner.adapter =
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                                opts)
                val popupBg = context.getDrawable(R.drawable.popup_bg_options) as LayerDrawable
                popupBg.findDrawableByLayerId(R.id.background_popup)
                        .setTint(MaterialPalette.get(context).cardBackgroud)
                view.spinner.setPopupBackgroundDrawable(popupBg)
                view.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if (selection.elementAtOrNull(position) != null)
                            selection.removeAt(position)
                        selection.add(position, opts[p2])
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                }
                val sel = selection.elementAtOrNull(position)
                if (sel != null && opts.contains(sel)) {
                    view.spinner.setSelection(opts.indexOf(sel))
                }
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