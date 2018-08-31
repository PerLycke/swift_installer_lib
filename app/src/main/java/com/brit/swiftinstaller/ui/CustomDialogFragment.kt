package com.brit.swiftinstaller.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

@Suppress("unused")
class CustomDialogFragment : androidx.fragment.app.DialogFragment() {

    private var mCreator: DialogCreator? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (mCreator != null) {
            return mCreator!!.dialog
        }
        throw IllegalArgumentException("DialogCreator is null!")
    }

    interface DialogCreator {
        val dialog: Dialog
    }

    companion object {

        fun showDialog(activity: AppCompatActivity, creator: DialogCreator) {
            val dialogFragment = newInstance(creator)
            dialogFragment.show(activity.supportFragmentManager, "custom")
        }

        fun showDialog(fragment: androidx.fragment.app.Fragment, creator: DialogCreator) {
            val dialogFragment = newInstance(creator)
            dialogFragment.setTargetFragment(fragment, 0)
            dialogFragment.show(fragment.fragmentManager, creator.toString())
        }

        private fun newInstance(creator: DialogCreator): CustomDialogFragment {
            val fragment = CustomDialogFragment()
            fragment.mCreator = creator
            return fragment
        }
    }
}
