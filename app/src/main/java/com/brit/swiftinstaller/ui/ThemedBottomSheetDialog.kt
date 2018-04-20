package com.brit.swiftinstaller.ui

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.getBackgroundColor

open class ThemedBottomSheetDialog(context: Context): BottomSheetDialog(context,
        if (getBackgroundColor(context) == 0x000000) {
            R.style.AppTheme_BottomSheetDialog_Black
        } else {
            R.style.AppTheme_BottomSheetDialog
        }) {

}