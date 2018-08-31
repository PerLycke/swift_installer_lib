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

package com.brit.swiftinstaller.ui.applist

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

class AppItem() : Parcelable {
    var packageName: String = ""
    var title: String = ""
    private var required: String = ""
    var versionCode: Long = 0
    var versionName: String = ""
    var icon: Drawable? = null

    constructor(parcel: Parcel) : this() {
        packageName = parcel.readString()!!
        title = parcel.readString()!!
        required = parcel.readString()!!
        versionCode = parcel.readLong()
        versionName = parcel.readString()!!
    }

    override fun describeContents(): Int {
        return describeContents()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(packageName)
        dest?.writeString(title)
        dest?.writeString(required)
        dest?.writeLong(versionCode)
        dest?.writeString(versionName)
    }

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel): AppItem {
            return AppItem(parcel)
        }

        override fun newArray(size: Int): Array<AppItem?> {
            return arrayOfNulls(size)
        }
    }
}