package com.wavez.base.notification.reengagement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arms the two daily re-engagement alarms with [AlarmManager.setAndAllowWhileIdle] —
 * inexact, no [android.Manifest.permission.SCHEDULE_EXACT_ALARM] /
 * [android.Manifest.permission.USE_EXACT_ALARM] needed, and (unlike WorkManager) still
 * fires after the user force-stops the app since AlarmManager alarms are a system-level
 * exception to that kill.
 *
 * Alarms are wiped by the OS on reboot, so [ensureScheduled] must also be called from a
 * BOOT_COMPLETED receiver.
 */
@Singleton
class ReEngagementScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ReEngagementReminder"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * (Re-)arms both slots unconditionally. Deliberately does NOT skip slots whose
     * `PendingIntent` already exists — that check only tells us the *registration* is still
     * around, not that the OS actually still intends to fire it. Some OEM battery managers
     * (e.g. MIUI without Autostart/"No restrictions" granted) silently drop the underlying
     * alarm without clearing the `PendingIntent`, which made the old exists-check report
     * "already armed" forever even though nothing was going to fire. Re-arming on every app
     * start is cheap (same target time, harmless overwrite) and self-heals from that case.
     */
    fun ensureScheduled() {
        Log.d(TAG, "ensureScheduled() called")
        ReminderSlot.entries.forEach { slot -> scheduleNext(slot) }
    }

    /** Arms [slot] for its next occurrence (today if not yet passed, else tomorrow). */
    fun scheduleNext(slot: ReminderSlot) {
        val triggerAt = nextTriggerTime(slot.hourOfDay)
        val pendingIntent = pendingIntentFor(slot)
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Log.d(TAG, "[Scheduler] Armed ${slot.name} for ${dateFormat.format(Date(triggerAt))}")
        } catch (e: Exception) {
            Log.e(TAG, "[Scheduler] Failed to arm ${slot.name}", e)
        }
    }

    private fun nextTriggerTime(hourOfDay: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!trigger.after(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }

    private fun pendingIntentFor(slot: ReminderSlot): PendingIntent {
        val intent = Intent(context, ReEngagementReminderReceiver::class.java).apply {
            putExtra(ReminderSlot.EXTRA_SLOT_NAME, slot.name)
        }
        return PendingIntent.getBroadcast(
            context,
            slot.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
