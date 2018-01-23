package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable

class CustomizeActivity : AppCompatActivity() {

    var settingsIcons: Array<ImageView?> = arrayOfNulls(3)
    lateinit var hexInput: EditText
    lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LayoutInflater.from(this).inflate(R.layout.activity_customize, null)
        setupAccentSheet(layout)
        setContentView(layout)
    }

    fun setupAccentSheet(view: View) {
        val paletteGrid = view.findViewById<GridView>(R.id.palette)
        paletteGrid.adapter = PaletteAdapter(this,
                resources.getIntArray(R.array.accent_colors))
        settingsIcons.set(0, view.findViewById<ImageView>(R.id.connectionsIcon))
        settingsIcons.set(1, view.findViewById<ImageView>(R.id.soundIcon))
        settingsIcons.set(2, view.findViewById<ImageView>(R.id.notificationsIcon))
        hexInput = view.findViewById(R.id.hexInput)
        applyButton = view.findViewById(R.id.applyHex)
    }

    fun updateColor(color: Int) {
        Log.d("TEST", "n - " + settingsIcons.size)
        for (icon: ImageView? in settingsIcons) {
            Log.d("TEST", "color - " + color)
            icon!!.setColorFilter(color)
        }
        hexInput.background.setTint(color)
        applyButton.setTextColor(color)
    }

    inner class PaletteAdapter constructor(context: Context, private val mColors: IntArray) : BaseAdapter() {

        override fun getCount(): Int {
            return mColors.size
        }

        override fun getItem(position: Int): Any {
            return mColors[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.palette_view, parent, false)
            }
            val iv = convertView!!.findViewById<ImageView>(R.id.icon)
            iv.background = CircleDrawable(mColors[position])
            convertView.tag = mColors[position]
            convertView.setOnClickListener { updateColor(mColors[position]) }
            return convertView
        }
    }

    fun blackBgClick(view: View) {
        val dialog = LayoutInflater.from(this).inflate(R.layout.background_alert_dialog, null)
        val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
        builder.setView(dialog)
        builder.show()
    }

    fun cancelBtnClick(view: View) {
        finish()
    }

}
