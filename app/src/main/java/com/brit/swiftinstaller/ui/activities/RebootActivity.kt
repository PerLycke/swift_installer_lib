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

package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.utils.rebootCommand

class RebootActivity : ThemeActivity() {

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("reboot_card", false).apply()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(getString(R.string.reboot_dialog_title))
                .setMessage(getString(R.string.reboot_dialog_msg))
                .setPositiveButton(getString(R.string.reboot)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val dialog = Dialog(this, R.style.AppTheme_Translucent)
                    dialog.setContentView(R.layout.reboot)
                    dialog.show()
                    mHandler.post {
                        rebootCommand()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    finish()
                }
                .setOnCancelListener {
                    finish()
                }

        themeDialog()
        val dialog = builder.create()
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}