package com.brit.swiftinstaller.library.ui.changelog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.brit.swiftinstaller.library.R
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
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.swift_app_name)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }

        val view = View.inflate(activity, R.layout.changelog_dialog, null)
        view.setBackgroundColor(activity!!.swift.selection.backgroundColor)
        val pb = view.findViewById<ProgressBar>(R.id.pbLoading)
        val rv = view.findViewById<RecyclerView>(R.id.rvChangelog)
        val adapter = builder!!.setupEmptyRecyclerView(rv)

        task = ChangelogParserAsyncTask(activity, pb, adapter, builder)
        task!!.execute()

        dialog.setView(view)
        val d = dialog.create()
        d.window!!.setBackgroundDrawable(ColorDrawable(activity!!.swift.selection.backgroundColor))
        d.setOnShowListener {
            d.setIcon(R.mipmap.ic_launcher)
            d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity!!.swift.selection.accentColor)
        }
        return d
    }

    override fun onDestroy() {
        if (task != null) {
            task!!.cancel(true)
        }
        super.onDestroy()
    }
}