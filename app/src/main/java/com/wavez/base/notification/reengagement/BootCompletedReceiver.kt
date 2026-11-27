package com.wavez.base.notification.reengagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Re-arms every alarm-based reminder after a reboot — the OS wipes every AlarmManager
 * alarm on restart, so without this the feature would silently stop firing until the user
 * next opens the app.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReEngagementReminder"
    }

    @Inject
    lateinit var scheduler: ReEngagementScheduler

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "[BootReceiver] onReceive fired, action=${intent.action}")
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            Log.d(TAG, "[BootReceiver] ignoring unrelated action")
            return
        }
        Log.d(TAG, "[BootReceiver] re-arming reminder alarms after reboot")
        scheduler.ensureScheduled()
    }
}
