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
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.activities.InstallActivity
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.checkVersionCompatible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.app_item.*
import kotlinx.android.synthetic.main.app_option_item.view.*

class AppListFragment : Fragment() {

    var apps: SynchronizedArrayList<AppItem> = SynchronizedArrayList()
    var visible: SynchronizedArrayList<Int> = SynchronizedArrayList()

    private var requiredApps: Array<String> = emptyArray()

    lateinit var appExtrasHandler: AppExtrasHandler

    private val checked = SparseBooleanArray()

    var alertIconClickListener: AlertIconClickListener? = null
    var appCheckBoxClickListener: AppCheckBoxClickListener? = null
    var viewClickListener: ViewClickListener? = null

    private var summary = false
    private var extras = false
    private var failedTab = false

    companion object {
        fun instance(summary: Boolean, extras: Boolean, failedTab: Boolean): AppListFragment {
            val fragment = AppListFragment()
            val args = Bundle()
            args.putBoolean("summary", summary)
            args.putBoolean("extras", extras)
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
            summary = arguments!!.getBoolean("summary", false)
            extras = arguments!!.getBoolean("extras", false)
            failedTab = arguments!!.getBoolean("failed_tab", false)
        }
        selectAll(false)
        return view
    }

    fun addCard(card: View) {
        view?.findViewById<LinearLayout>(R.id.app_list_root)?.addView(card, 0)
    }

    fun querySearch(query: String) {
        visible.clear()
        if (query.isEmpty() || query.isBlank()) {
            apps.forEach { visible.add(apps.indexOf(it)) }
        } else {
            apps.forEach {
                if (it.title.toLowerCase().contains(query.toLowerCase())) {
                    visible.add(apps.indexOf(it))
                }
            }
        }
        if (app_list_view != null && !app_list_view.isComputingLayout) {
            app_list_view.adapter?.notifyDataSetChanged()
        }
    }

    inner class CachingLayoutManager: LinearLayoutManager(context) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }

        override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
            return 400
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app_list_view.adapter = AppAdapter()
        app_list_view.layoutManager = CachingLayoutManager().apply {
            isItemPrefetchEnabled = true
        }
        app_list_view.setHasFixedSize(true)
        app_list_view.setItemViewCacheSize(apps.size)
        app_list_view.isNestedScrollingEnabled = false
    }

    fun getCheckedItems(): SynchronizedArrayList<AppItem> {
        val checkedApps = SynchronizedArrayList<AppItem>()
        for (i in apps.indices) {
            if (checked.get(i) || requiredApps.contains(apps[i].packageName)) {
                checkedApps.add(apps[i])
            }
        }
        return checkedApps
    }

    fun clearCheckedITems() {
        checked.clear()
    }

    fun addApp(app: AppItem) {
        apps.add(app)
        visible.clear()
        visible.addAll(apps.indices)
        app_list_view?.let {
            if (!it.isComputingLayout) {
                it.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun selectAll(check: Boolean) {
        for (index in apps.indices) {
            if (context != null) {
                if (checkVersionCompatible(context!!, apps[index].packageName) ||
                        context!!.swift.romHandler.isOverlayInstalled(apps[index].packageName)) {
                    checked.put(index, check)
                }
            } else {
                checked.put(index, check)
            }
        }
    }

    fun setAppList(newApps: SynchronizedArrayList<AppItem>) {
        apps.clear()
        visible.clear()
        apps.addAll(newApps)
        visible.addAll(apps.indices)
        if (app_list_view != null && !app_list_view.isComputingLayout) {
            app_list_view.adapter?.notifyDataSetChanged()
        }
    }

    fun setRequiredAppList(apps: Array<String>) {
        requiredApps = apps
    }
    
    fun notifyDataSetChanged() {
        if (app_list_view != null && !app_list_view.isComputingLayout) {
            app_list_view.adapter?.notifyDataSetChanged()
        }
    }

    inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(
                    R.layout.app_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (apps.isNotEmpty()) {
                holder.bindAppItem(apps[visible[position]])
            }

            if (position + 1 == itemCount) {
                setBottomMargin(holder.itemView,
                        (64 * Resources.getSystem().displayMetrics.density).toInt())
            } else {
                setBottomMargin(holder.itemView, 0)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return 0
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
            return visible.size
        }

        inner class ViewHolder(override val containerView: View) :
                RecyclerView.ViewHolder(containerView), LayoutContainer {

            private val checkListener: (CompoundButton, Boolean) -> Unit
            private val clickListener: (View) -> Unit = {
                viewClickListener!!.onClick(apps[visible[adapterPosition]])
                app_item_checkbox.toggle()
            }

            init {
                checkListener = { _, check ->
                    checked.put(visible[adapterPosition], check)
                }
                containerView.setOnClickListener(clickListener)
                app_item_checkbox.setOnClickListener {
                    appCheckBoxClickListener!!.onCheckBoxClick(apps[visible[adapterPosition]])
                }
                app_item_checkbox.setOnCheckedChangeListener(checkListener)
                alert_icon.setOnClickListener {
                    alertIconClickListener!!.onAlertIconClick(apps[visible[adapterPosition]])
                }
                night_icon.setOnClickListener {
                    containerView.context.alert {
                        title = "Dark mode required"
                        message = OverlayUtils.getNightInfo(context!!, apps[visible[adapterPosition]].packageName)
                        positiveButton("Open App") { d ->
                            d.dismiss()
                            activity!!.startActivity(activity!!.pm.getLaunchIntentForPackage(apps[visible[adapterPosition]].packageName))
                        }
                        negativeButton("Dismiss") { d ->
                            d.dismiss()
                        }
                        show()
                    }
                }
            }

            fun bindAppItem(item: AppItem) {
                app_item_name.text = item.title
                app_item_name.setTextColor(context!!.getColor(android.R.color.white))
                app_item_name.alpha = 1.0f
                app_item_image.setImageDrawable(item.icon)
                app_name.text = item.packageName
                app_item_checkbox.visibility = View.VISIBLE
                app_item_checkbox.isChecked = checked.get(visible[adapterPosition], false)
                app_item_checkbox.alpha = 1.0f
                alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_info))
                alert_icon.setVisible(item.hasVersions)
                required.visibility = View.GONE
                download_icon.visibility = View.GONE
                blocked_packages_alert.visibility = View.GONE
                options_icon.setVisible(false)
                containerView.isClickable = true

                item.appOptions?.let { appOptions ->
                    if (appOptions.isNotEmpty() && !summary) {
                        val optionsSelection = SynchronizedArrayList<String>()
                        val selected = getSelectedOverlayOptions(context!!, item.packageName)
                        for (i in appOptions.keys.indices) {
                            optionsSelection.add(i, selected[appOptions.keyAt(i)] ?: "")
                        }
                        options_icon.visibility = View.VISIBLE
                        options_icon.setColorFilter(
                                activity!!.swift.romHandler.getCustomizeHandler().getSelection().accentColor)

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
                                    if (item.installed) {
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
                    }
                    if (extras) {
                        app_item_checkbox.isClickable = false
                        app_item_checkbox.setVisible(false)
                        app_name.setVisible(false)
                        alert_icon.setVisible(false)
                        containerView.isClickable = false
                    }
                }

                if (summary) {
                    app_item_checkbox.visibility = View.GONE
                    app_item_checkbox.isEnabled = false
                    containerView.isClickable = false
                    if (failedTab) {
                        alert_icon.visibility = View.VISIBLE
                        alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                    }
                } else {
                    if (item.isRequired && !item.installed) {
                        app_item_checkbox.isChecked = true
                        app_item_checkbox.isClickable = false
                        app_item_checkbox.alpha = 0.3f
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.required)
                        app_item_name.setTextColor(Color.parseColor("#4dffffff"))
                        containerView.isClickable = false
                    }
                    if (item.incompatible) {
                        app_item_name.alpha = 0.3f
                        alert_icon.setImageDrawable(context!!.getDrawable(R.drawable.ic_alert))
                        required.visibility = View.VISIBLE
                        required.text = getString(R.string.unsupported)
                        if (!item.installed) {
                            app_item_checkbox.visibility = View.GONE
                            app_item_checkbox.isClickable = false
                            app_item_checkbox.isChecked = false
                            containerView.isClickable = false
                            required.visibility = View.GONE
                        }
                    }
                    if (item.hasUpdate && item.installed) {
                        app_item_name.setTextColor(context!!.getColor(R.color.minimal_orange))
                    }
                    if (app_item_name.text.contains("Samsung Music") || app_item_name.text.contains(
                                    "Voice Recorder")) {
                        blocked_packages_alert.visibility = View.VISIBLE
                        blocked_packages_alert.setColorFilter(context!!.swift.selection.accentColor)
                    }

                    night_icon.setVisible(item.nightAvailable)
                    night_icon.isClickable = item.nightAvailable
                    night_icon.setColorFilter(context!!.swift.selection.accentColor)
                }

                if (appExtrasHandler.appExtras.containsKey(item.packageName)) {
                    when {
                        getHiddenApps(context!!).contains(item.packageName) -> {
                            app_item_checkbox.isClickable = false
                            app_item_checkbox.setVisible(false)
                        }
                        summary -> {
                            required.visibility = View.VISIBLE
                            required.text = getString(R.string.needs_extras)
                        }
                        else -> {
                            if (extras) {
                                app_item_checkbox.isClickable = false
                                app_item_checkbox.setVisible(false)
                                app_name.setVisible(false)
                            }
                            download_icon.visibility = View.VISIBLE
                            download_icon.setColorFilter(context!!.swift.selection.accentColor)
                            download_icon.setOnClickListener {
                                appExtrasHandler.appExtras[item.packageName]?.invoke(
                                        activity!! as AppCompatActivity)
                            }
                        }
                    }
                }
            }
        }

    }

    class OptionsAdapter(context: Context, val options: ArrayMap<String, Array<String>>,
                         val selection: SynchronizedArrayList<String>) :
            ArrayAdapter<String>(context, R.layout.app_option_item) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.app_option_item,
                    parent, false)

            view.options_title.text = options.keyAt(position)
            val opts = options[options.keyAt(position)]
            if (opts!!.contains("on")) {
                view.checkbox.visibility = View.VISIBLE
                view.checkbox.isChecked = (selection.elementAtOrNull(position) ?: "off") == "on"
                view.checkbox.setOnCheckedChangeListener { _, b ->
                    selection[position] = if (b) {
                        "on"
                    } else {
                        "off"
                    }
                }
                view.spinner.visibility = View.GONE
            } else {
                view.spinner.visibility = View.VISIBLE
                view.checkbox.visibility = View.GONE
                view.spinner.adapter =
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                                opts)
                val popupBg = context.getDrawable(R.drawable.popup_bg_options) as LayerDrawable
                popupBg.findDrawableByLayerId(R.id.background_popup)
                        .setTint(MaterialPalette.get(context).cardBackground)
                view.spinner.setPopupBackgroundDrawable(popupBg)
                view.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        selection[position] = opts[p2]
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