package com.wavez.base.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Locale

object DeviceUtils {

    private fun isDeveloperModeEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * USB Debugging đang bật
     */
    private fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Chỉ cần một trong hai bật thì trả về true
     */
    fun isDevEnvironment(context: Context): Boolean {
        return isDeveloperModeEnabled(context) ||
            isUsbDebuggingEnabled(context)
    }
}
