package com.brit.swiftinstaller.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.Image
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.BottomSheetDialog
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.*
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.ui.CircleDrawable
import com.brit.swiftinstaller.utils.getAccentColor
import kotlinx.android.synthetic.main.accent_sheet.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    var settingsIcons: Array<ImageView?> = arrayOfNulls(3)
    lateinit var hexInput: EditText
    lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        installTile.setOnClickListener {
            startActivity(Intent(this, OverlayActivity::class.java))
        }

        accentTile.setOnClickListener {
            val sheetView = LayoutInflater.from(this).inflate(R.layout.accent_sheet, null)
            setupAccentSheet(sheetView);
            val mBottomSheetDialog = BottomSheetDialog(this)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.show()
        }
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

    override fun onResume() {
        super.onResume()
        currentAccent.setTextColor(getAccentColor(this))
        currentAccent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_about -> {
                val builder = AlertDialog.Builder(this, R.style.AppAlertDialogTheme).create()
                builder.setTitle("About")
                builder.setMessage("This is about")
                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
}
