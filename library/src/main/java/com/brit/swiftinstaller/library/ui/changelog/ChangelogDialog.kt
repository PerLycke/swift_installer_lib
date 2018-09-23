package com.brit.swiftinstaller.library.ui.changelog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.swift
import com.michaelflisar.changelog.ChangelogBuilder
import com.michaelflisar.changelog.internal.ChangelogParserAsyncTask
import kotlinx.android.synthetic.main.swift_changelog_dialog.view.*

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
        val dialog = AlertDialog.Builder(activity!!)
                .setTitle(R.string.swift_app_name)
                .setIcon(R.mipmap.ic_launcher)

        val view = View.inflate(activity, R.layout.swift_changelog_dialog, null)
        val pb = view.findViewById<ProgressBar>(R.id.pbLoading)
        val rv = view.findViewById<RecyclerView>(R.id.rvChangelog)
        val adapter = builder!!.setupEmptyRecyclerView(rv)

        view.btn_ok.setTextColor(context!!.swift.selection.accentColor)
        view.btn_ok.setOnClickListener {
            getDialog().dismiss()
        }

        task = ChangelogParserAsyncTask(activity, pb, adapter, builder)
        task!!.execute()

        dialog.setView(view)
        val d = dialog.create()
        val dialogBg = context?.getDrawable(R.drawable.dialog_bg) as LayerDrawable
        dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(context!!.swift.selection.backgroundColor)
        d.window!!.setBackgroundDrawable(dialogBg)
        return d
    }

    override fun onDestroy() {
        if (task != null) {
            task!!.cancel(true)
        }
        super.onDestroy()
    }
}