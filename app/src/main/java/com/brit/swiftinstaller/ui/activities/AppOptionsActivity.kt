package com.brit.swiftinstaller.ui.activities

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.utils.OverlayUtils
import com.brit.swiftinstaller.utils.Utils
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.activity_app_options.*
import kotlinx.android.synthetic.main.app_option_item.view.*

class AppOptionsActivity : ThemeActivity() {

    var mApps: ArrayList<AppItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_options)

        for (app in Utils.sortedOverlaysList(this)) {
            Log.d("TEST", "app - ${app.packageName}")
            if (OverlayUtils.hasOverlayOptions(this, app.packageName)) {
                Log.d("TEST", "has options")
                mApps.add(app)
            }
        }

        app_options_view.adapter = AppAdapter()
        app_options_view.layoutManager = LinearLayoutManager(this)

    }

    inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(this@AppOptionsActivity).inflate(
                    R.layout.app_options_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindAppItem(mApps[position])

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
            return mApps.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private var appName: TextView = view.findViewById(R.id.app_item_name)
            private var appIcon: ImageView = view.findViewById(R.id.app_item_image)
            private var options: LinearLayout = view.findViewById(R.id.app_options)

            fun bindAppItem(item: AppItem) {
                appName.text = item.title
                appName.setTextColor(getColor(android.R.color.white))
                appName.alpha = 1.0f
                appIcon.setImageDrawable(item.icon)

                val ops = OverlayUtils.getOverlayOptions(applicationContext, item.packageName)
                for (name in ops.keys) {
                    val layout = layoutInflater.inflate(R.layout.app_option_item, options, false)
                    layout.options_title.text = name
                    val opVals = ops[name] ?: emptyArray()
                    if (opVals.contains("on")) {
                        layout.spinner.visibility = View.GONE
                        layout.checkbox.visibility = View.VISIBLE
                    } else {
                        layout.checkbox.visibility = View.GONE
                        layout.spinner.visibility = View.VISIBLE

                        layout.spinner.adapter = ArrayAdapter<String>(itemView.context, R.layout.spinner_item, opVals)
                    }
                    options.addView(layout)
                }
            }
        }
    }
}