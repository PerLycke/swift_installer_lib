package com.brit.swiftinstaller.ui.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.activities.AppListActivity

class AppListAdapter(val activity: AppListActivity, val packages: MutableList<ApplicationInfo>) :
        RecyclerView.Adapter<AppListAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.app_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder?, position: Int) {
        holder?.bindApp(activity, packages[position])
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private var appItemImage: ImageView? = itemView?.findViewById(R.id.appItemImage)
        private var appItemName: TextView? = itemView?.findViewById(R.id.appItemName)
        private var appItemSelected: CheckBox? = itemView?.findViewById(R.id.appItemCheckBox)

        fun bindApp(context: Context, appInfo: ApplicationInfo) {
            appItemImage?.setImageDrawable(appInfo.loadIcon(context.packageManager))
            appItemName?.text = appInfo.loadLabel(context.packageManager)
            itemView.setOnClickListener {
                appItemSelected?.isChecked = appItemSelected?.isChecked != true
            }
            appItemSelected?.isChecked = activity.apps.contains(appInfo)
            appItemSelected?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    activity.apps.add(appInfo)
                    Toast.makeText(context, "${activity.apps}", Toast.LENGTH_SHORT).show()
                } else {
                    activity.apps.remove(appInfo)
                    Toast.makeText(context, "${activity.apps}", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}