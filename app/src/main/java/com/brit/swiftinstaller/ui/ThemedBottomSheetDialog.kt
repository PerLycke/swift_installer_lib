package com.brit.swiftinstaller.ui

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.useBlackBackground

open class ThemedBottomSheetDialog(context: Context): BottomSheetDialog(context,
        if (useBlackBackground(context)) {
            R.style.CustomBottomSheetDialogTheme_Black
        } else {
            R.style.CustomBottomSheetDialogTheme
        }) {

}