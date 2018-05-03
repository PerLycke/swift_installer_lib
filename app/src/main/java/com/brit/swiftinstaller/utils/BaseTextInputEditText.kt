package com.brit.swiftinstaller.utils

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo

class BaseTextInputEditText(context: Context?, attrs: AttributeSet?) : TextInputEditText(context, attrs){

    var firstFocus = true

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (hasFocus() && firstFocus) {
            setText("")
            firstFocus = false
        }
        if (!focused) this.hideKeyboard()
    }
}