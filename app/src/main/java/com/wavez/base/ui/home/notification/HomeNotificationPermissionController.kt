package com.wavez.base.ui.home.notification

import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import com.wavez.base.storage.SharedPref
import com.wavez.base.utils.AppState
import com.wavez.base.utils.permission.PermissionManager
import java.util.Calendar

/**
 * Drives the notification-permission asks surfaced from the Main screen:
 *   - very first time the user ever reaches Main: the OS system permission dialog, once ever.
 *     Allow or deny, the user can use the app right away and won't be asked again this session.
 *   - every later time: the app's own dialog ([com.wavez.base.ui.home.dialog.NotiPerDialogVer2]),
 *     only right after a successful playlist add or the user taps a channel to watch (flag is set
 *     at tap time, not on successful playback) sends the user back to Main, capped at once per
 *     calendar day.
 */
class HomeNotificationPermissionController(
    private val sharedPref: SharedPref,
    private val permissionManager: PermissionManager
) {

    /** Call once, the very first time Main is ever shown. No-op on every call after. */
    fun requestFirstLaunchPermissionIfNeeded(permissionLauncher: ActivityResultLauncher<String>) {
        if (sharedPref.hasDecidedNotificationPermission) return
        sharedPref.hasDecidedNotificationPermission = true
        AppState.hasHandledFirstLaunchNotificationPrompt = true
        if (permissionManager.hasNotificationPermission()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sharedPref.hasRequestedNotificationPermission = true
            permissionManager.requestNotificationPermission(permissionLauncher)
        }
    }

    /**
     * Call on every later Main resume. Returns true if the caller should show the app's
     * own permission dialog right now.
     */
    fun shouldShowAppDialogForPendingTrigger(): Boolean {
        if (!AppState.pendingNotificationPermissionPrompt) return false
        AppState.pendingNotificationPermissionPrompt = false
        if (permissionManager.hasNotificationPermission()) return false
        if (AppState.hasHandledFirstLaunchNotificationPrompt) return false
        val now = System.currentTimeMillis()
        if (isSameDay(sharedPref.lastNotificationPermissionDialogShownAt, now)) return false
        sharedPref.lastNotificationPermissionDialogShownAt = now
        return true
    }

    private fun isSameDay(a: Long, b: Long): Boolean {
        if (a <= 0L) return false
        val calA = Calendar.getInstance().apply { timeInMillis = a }
        val calB = Calendar.getInstance().apply { timeInMillis = b }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }
}
