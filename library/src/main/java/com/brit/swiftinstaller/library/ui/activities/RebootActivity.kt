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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.*

class RebootActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs.edit().putBoolean("reboot_card", false).apply()

        alert {
            title = getString(R.string.reboot_dialog_title)
            message = if (intent.hasExtra("message")) {
                intent.getStringExtra("message")
            } else {
                getString(R.string.reboot_dialog_msg)
            }
            positiveButton(R.string.reboot) {
                val rebootingDialog = Dialog(ctx, R.style.AppTheme_Translucent)
                rebootingDialog.setContentView(R.layout.reboot)
                rebootingDialog.show()
                val handler = MessageQueue.IdleHandler {
                    val force = intent.getBooleanExtra("force-reboot", false)
                    if (!force || getUseSoftReboot(ctx)) {
                        quickRebootCommand()
                    } else {
                        rebootCommand()
                    }
                    false
                }
                Looper.myQueue().addIdleHandler(handler)
            }
            negativeButton(R.string.cancel) { dialog ->
                dialog.dismiss()
                finish()
            }
            onCancelled {
                finish()
            }
            show()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    companion object {
        fun launchRebootActivity(context: Context, message: String, forceReboot: Boolean) {
            val intent = Intent(context, RebootActivity::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("message", message)
            intent.putExtra("force-reboot", forceReboot)
            context.startActivity(intent)
        }
    }
}