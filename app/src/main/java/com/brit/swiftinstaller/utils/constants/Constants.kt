package com.brit.swiftinstaller.utils.constants

import android.os.Process.myUid


val SIMULATE_INSTALL = false
val INSTALL_FAILED_INCOMPATIBLE = 1001
val CURRENT_USER = myUid() / 100000

val ACTION_KNOX_LICENSE_STATUS = "edm.intent.action.knox_license.status"
val ACTION_LICENSE_STATUS = "edm.intent.action.license.status"

val MDM_APP_MGMT_PERM = "android.permission.sec.MDM_APP_MGMT"
val MDM_SECURITY = "android.permission.sec.MDM_SERCURITY"