package com.brit.swiftinstaller.library.ui.preference

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.getAccentColor

class SwiftSwitchPreference(context: Context, attrs: AttributeSet) : SwitchPreference(context, attrs) {

    var switch: Switch? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        switch = findSwitchInChildViews(holder.itemView as ViewGroup)
        if (switch != null) {
            changeColor(switch!!.isChecked, switch!!.isEnabled)
        }
    }

    private fun changeColor(checked: Boolean, enabled: Boolean) {
        if (enabled) {
            val color = if (checked) { getAccentColor(context) } else { Color.LTGRAY }
            if (switch != null) {
                switch!!.thumbDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                switch!!.trackDrawable.setColorFilter(ColorUtils.addAlphaColor(color, 70), PorterDuff.Mode.MULTIPLY)
            }
        }
    }

    private fun findSwitchInChildViews(view: ViewGroup): Switch? {
        for (i in 0.rangeTo(view.childCount)) {
            val cv = view.getChildAt(i)
            if (cv is Switch) {
                return cv
            } else if (cv is ViewGroup) {
                val s = findSwitchInChildViews(cv)
                if (s != null) return s
            }
        }
        return null
    }

}