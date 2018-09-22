package com.brit.swiftinstaller.library.ui.preference

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.brit.swiftinstaller.library.utils.ColorUtils
import com.brit.swiftinstaller.library.utils.swift

class SwiftSwitchPreference(context: Context, attrs: AttributeSet) :
        SwitchPreference(context, attrs) {

    var switch: Switch? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        switch = findSwitchInChildViews(holder.itemView as ViewGroup)
        switch?.let {
            changeColor(it.isChecked, it.isEnabled)
        }
    }

    private fun changeColor(checked: Boolean, enabled: Boolean) {
        if (enabled) {
            val color = if (checked) {
                context.swift.selection.accentColor
            } else {
                Color.LTGRAY
            }
            switch?.let {
                it.thumbDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                it.trackDrawable.setColorFilter(ColorUtils.addAlphaColor(color, 10),
                        PorterDuff.Mode.MULTIPLY)
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