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
import android.os.Looper
import android.os.MessageQueue
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.utils.*

class RebootActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs.edit().putBoolean("reboot_card", false).apply()

        alert {
            if (swift.romHandler.neverReboot()) {
                title = getString(R.string.restart_sysui)
                message = getString(R.string.restart_sysui_msg)
            } else {
                title = getString(R.string.reboot_dialog_title)
                message = getString(R.string.reboot_dialog_msg)
            }
            positiveButton(if (swift.romHandler.neverReboot()) { R.string.restart_sysui } else { R.string.reboot }) {
                val rebootingDialog = Dialog(ctx, R.style.AppTheme_Translucent)
                rebootingDialog.setContentView(R.layout.reboot)
                rebootingDialog.show()
                val handler = MessageQueue.IdleHandler {
                    if (swift.romHandler.neverReboot()) {
                        restartSysUi(ctx)
                        prefs.edit().putBoolean("hotswap", false).apply()
                        finish()
                    } else {
                        if (getUseSoftReboot(ctx)) {
                            quickRebootCommand()
                        } else {
                            rebootCommand()
                        }
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
}