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

package com.brit.swiftinstaller.library.ui.customize

import androidx.collection.ArrayMap
import org.json.JSONObject

class CustomizeSelection : ArrayMap<String, String>() {

    var accentColor
        get() = get("accent").toInt()
        set(v) {
            put("accent", v.toString())
        }

    var backgroundColor
        get() = get("background").toInt()
        set(v) {
            put("background", v.toString())
        }

    override fun toString(): String {
        val json = JSONObject()
        for (key in keys) {
            json.put(key, get(key))
        }
        return json.toString()
    }

    override fun get(key: String?): String {
        val s = super.get(key)
        return if (s.isNullOrEmpty()) {
            ""
        } else {
            s!!
        }
    }

    fun getInt(key: String): Int {
        val s = super.get(key)
        return if (s.isNullOrEmpty()) {
            0
        } else {
            try {
                s!!.toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }
    }

    companion object {
        fun fromString(json: String): CustomizeSelection {
            if (json.isEmpty()) return CustomizeSelection()
            val selection = CustomizeSelection()
            val jsonObject = JSONObject(json)
            for (key in jsonObject.keys()) {
                selection[key] = jsonObject.getString(key)
            }
            return selection
        }
    }
}