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

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.ShellUtils
import com.brit.swiftinstaller.library.utils.getUseSoftReboot
import com.brit.swiftinstaller.library.utils.quickRebootCommand
import com.brit.swiftinstaller.library.utils.rebootCommand
import com.brit.swiftinstaller.library.utils.swift

class UninstallFinishedActivity : ThemeActivity() {

    private lateinit var dialog: AlertDialog
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstallActivity().finish()

        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(R.string.reboot)
                .setMessage(R.string.reboot_manually)
        if (!ShellUtils.isRootAvailable) {
            builder.setPositiveButton(R.string.reboot_later) { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }
        } else {
            builder.setPositiveButton("Reboot Now") { _, _ ->
                val dialog = Dialog(this, R.style.AppTheme_Translucent)
                dialog.setContentView(R.layout.reboot)
                dialog.show()
                handler.post {
                    if (!swift.romInfo.magiskEnabled() && getUseSoftReboot(this)) {
                        quickRebootCommand()
                    } else {
                        rebootCommand()
                    }
                }
            }
            builder.setNegativeButton("Reboot Later") { _, _ ->
                finish()
            }
        }

        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        themeDialog()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            dialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}