package com.brit.swiftinstaller.ui.applist

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

class AppItem() : Parcelable {
    var packageName: String = ""
    var title: String = ""
    var required: String = ""
    var versionCode: Int = 0
    var versionName: String = ""
    var icon: Drawable? = null

    constructor(parcel: Parcel) : this() {
        packageName = parcel.readString()
        title = parcel.readString()
        required = parcel.readString()
        versionCode = parcel.readInt()
        versionName = parcel.readString()
    }

    override fun describeContents(): Int {
        return describeContents()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(packageName)
        dest?.writeString(title)
        dest?.writeString(required)
        dest?.writeInt(versionCode)
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