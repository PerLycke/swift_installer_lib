package com.brit.swiftinstaller.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager

class BaseTextInputEditText(context: Context?, attrs: AttributeSet) : AppCompatEditText(context, attrs){

    private val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (!focused) {
            inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    override fun onKeyPreIme(keyCode:Int, event:KeyEvent):Boolean {
        return super.dispatchKeyEvent(event)
    }
}