package com.brit.swiftinstaller.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

@Suppress("unused")
class CustomDialogFragment : DialogFragment() {

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

        fun showDialog(fragment: Fragment, creator: DialogCreator) {
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
