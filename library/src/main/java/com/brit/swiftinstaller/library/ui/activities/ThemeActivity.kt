/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library.ui.activities

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.IdLists
import com.brit.swiftinstaller.library.utils.KEY_BACKGROUND_PALETTE
import com.brit.swiftinstaller.library.utils.MaterialPalette
import com.brit.swiftinstaller.library.utils.swift
import com.brit.swiftinstaller.library.utils.useBackgroundPalette
import com.google.android.material.floatingactionbutton.FloatingActionButton

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener { _, key ->
                    if (key == "customize_options" || key == KEY_BACKGROUND_PALETTE) {
                        updateColors(swift.selection.backgroundColor, useBackgroundPalette(this))
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        updateColors(swift.selection.backgroundColor, useBackgroundPalette(this))
    }

    fun themeDialog() {
        val dialogBg = getDrawable(R.drawable.dialog_bg) as LayerDrawable
        dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(swift.selection.backgroundColor)
    }

    fun updateColors(backgroundColor: Int, usePalette: Boolean) {
        val palette = MaterialPalette.createPalette(backgroundColor, usePalette)
        window.statusBarColor = palette.backgroundColor
        window.navigationBarColor = palette.backgroundColor
        for (id in IdLists.bgIds) {
            val v = findViewById<View>(id)
            if (v != null) {
                v.background = ColorDrawable(palette.backgroundColor)
            }
        }
        if (findViewById<FloatingActionButton>(R.id.fab) != null) {
            findViewById<FloatingActionButton>(R.id.fab).background.setTint(
                    swift.selection.accentColor)
        }
        for (id in IdLists.cardIds) {
            val v = findViewById<View>(id)
            if (v != null) {
                if (v.background != null) {
                    v.background.setTint(palette.cardBackgroud)
                } else if (v is ImageView && v.drawable != null) {
                    if (v.drawable is StateListDrawable) {
                        val draw = v.drawable as StateListDrawable
                        draw.current.setTint(palette.cardBackgroud)
                    } else if (v.drawable is LayerDrawable) {
                        val draw = v.drawable as LayerDrawable
                        draw.findDrawableByLayerId(R.id.background).setTint(palette.cardBackgroud)
                    }
                }
            }
        }
    }

    fun setCursorPointerColor(view: EditText, color: Int) {
        try {
            var field = TextView::class.java.getDeclaredField("mTextSelectHandleRes")
            field.isAccessible = true
            val drawableResId = field.getInt(view)
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(view)
            val drawable = view.context.getDrawable(drawableResId)!!
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            field = editor.javaClass.getDeclaredField("mSelectHandleCenter")
            field.isAccessible = true
            field.set(editor, drawable)
        } catch (ex: Exception) {
        }
    }

    fun setCursorDrawableColor(view: EditText, color: Int) {
        try {
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(view)
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(view)
            val drawable = arrayOfNulls<Drawable>(2)
            drawable[0] = view.context.getDrawable(drawableResId)
            drawable[1] = view.context.getDrawable(drawableResId)
            drawable[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            drawable[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawable)
        } catch (ex: Exception) {
        }
    }
}