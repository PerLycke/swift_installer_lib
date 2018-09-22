package com.brit.swiftinstaller.library.ui.changelog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.alert
import com.brit.swiftinstaller.library.utils.swift
import com.michaelflisar.changelog.ChangelogBuilder
import com.michaelflisar.changelog.internal.ChangelogParserAsyncTask

class ChangelogDialog : DialogFragment() {

    private var task: ChangelogParserAsyncTask? = null

    companion object {
        fun show(activity: AppCompatActivity, builder: ChangelogBuilder) {
            val args = Bundle()
            args.putParcelable("builder", builder)
            val dlg = ChangelogDialog()
            dlg.arguments = args
            dlg.show(activity.supportFragmentManager, "changelog")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = arguments?.getParcelable<ChangelogBuilder>("builder")

        return context!!.alert {
            title = getString(R.string.swift_app_name)
            positiveButton(R.string.ok) { dialog ->
                dialog.dismiss()
            }

            val view = View.inflate(activity, R.layout.changelog_dialog, null)
            view.setBackgroundColor(activity!!.swift.selection.backgroundColor)
            val pb = view.findViewById<ProgressBar>(R.id.pbLoading)
            val rv = view.findViewById<RecyclerView>(R.id.rvChangelog)
            val adapter = builder!!.setupEmptyRecyclerView(rv)

            task = ChangelogParserAsyncTask(activity, pb, adapter, builder)
            task!!.execute()

            customView = view
        }.build()
    }

    override fun onDestroy() {
        if (task != null) {
            task!!.cancel(true)
        }
        super.onDestroy()
    }
}