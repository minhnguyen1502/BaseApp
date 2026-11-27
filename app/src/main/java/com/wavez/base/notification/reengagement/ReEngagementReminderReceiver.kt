package com.wavez.base.notification.reengagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wavez.base.R
import com.wavez.base.extension.localizedContext
import com.wavez.base.notification.DailyNotificationGate
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.splash.SplashActivity
import com.wavez.base.utils.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Fires on each armed slot. Always re-arms the slot for its next occurrence first —
 * that's what keeps the chain alive without WorkManager or AlarmManager.setRepeating —
 * then decides, purely locally, whether to actually show a notification this time.
 */
@AndroidEntryPoint
class ReEngagementReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReEngagementReminder"
        private const val CHANNEL_ID = "daily_reminder"
        private const val CHANNEL_NAME = "Daily reminders"
        private val MIN_GAP_SINCE_APP_OPEN = TimeUnit.HOURS.toMillis(5)
    }

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    lateinit var scheduler: ReEngagementScheduler

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var dailyNotificationGate: DailyNotificationGate

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "[Receiver] onReceive fired, action=${intent.action}, extra=${intent.getStringExtra(ReminderSlot.EXTRA_SLOT_NAME)}")
        val slot = ReminderSlot.fromName(intent.getStringExtra(ReminderSlot.EXTRA_SLOT_NAME))
        if (slot == null) {
            Log.w(TAG, "[Receiver] Unknown slot, ignoring")
            return
        }

        // Re-arm for the next day regardless of whether we show a notification below.
        scheduler.scheduleNext(slot)

        if (!permissionManager.hasNotificationPermission()) {
            Log.d(TAG, "[Receiver] ${slot.name}: skipped, no notification permission")
            return
        }

        val sinceLastOpenMs = System.currentTimeMillis() - sharedPref.lastAppOpenTimestamp
        val sinceLastOpenMinutes = TimeUnit.MILLISECONDS.toMinutes(sinceLastOpenMs)
        if (sinceLastOpenMs < MIN_GAP_SINCE_APP_OPEN) {
            Log.d(TAG, "[Receiver] ${slot.name}: skipped, app opened ${sinceLastOpenMinutes}min ago (< 5h)")
            return
        }
        Log.d(TAG, "[Receiver] ${slot.name}: gate passed, app opened ${sinceLastOpenMinutes}min ago")

        if (!dailyNotificationGate.tryConsume()) {
            Log.d(TAG, "[Receiver] ${slot.name}: skipped, daily notification cap reached")
            return
        }

        showNotification(context, slot)
    }

    private fun showNotification(context: Context, slot: ReminderSlot) {
        val lastIndex = when (slot) {
            ReminderSlot.NOON -> sharedPref.lastNoonReminderIndex
            ReminderSlot.EVENING -> sharedPref.lastEveningReminderIndex
        }
        val candidates = slot.titleBodyRes.indices.filter { it != lastIndex }
        val index = candidates.random()
        when (slot) {
            ReminderSlot.NOON -> sharedPref.lastNoonReminderIndex = index
            ReminderSlot.EVENING -> sharedPref.lastEveningReminderIndex = index
        }

        val (titleRes, bodyRes) = slot.titleBodyRes[index]

        createNotificationChannel(context)
        val localizedContext = context.localizedContext(sharedPref.configLanguage)

        val contentIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            slot.notificationId,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notify)
            .setContentTitle(localizedContext.getString(titleRes))
            .setContentText(localizedContext.getString(bodyRes))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(slot.notificationId, notification)
        Log.d(TAG, "[Receiver] ${slot.name}: shown, index=$index (previous=$lastIndex), title=\"${localizedContext.getString(titleRes)}\"")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}
