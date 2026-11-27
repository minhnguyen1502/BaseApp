package com.wavez.base.notification

import com.wavez.base.storage.SharedPref
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caps the number of re-engagement ([com.wavez.base.notification.reengagement.ReEngagementReminderReceiver])
 * notifications shown per calendar day (device local time) to [MAX_PER_DAY], regardless of
 * how many are otherwise eligible to fire that day.
 */
@Singleton
class DailyNotificationGate @Inject constructor(
    private val sharedPref: SharedPref
) {

    companion object {
        const val MAX_PER_DAY = 2
        private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    /**
     * Returns true and reserves a slot if fewer than [MAX_PER_DAY] notifications have been
     * shown today; returns false (reserving nothing) once the cap is reached. Resets the
     * count when the local calendar day rolls over.
     */
    @Synchronized
    fun tryConsume(): Boolean {
        val today = dayFormat.format(Date())
        if (sharedPref.notificationGateDate != today) {
            sharedPref.notificationGateDate = today
            sharedPref.notificationGateCount = 0
        }
        if (sharedPref.notificationGateCount >= MAX_PER_DAY) {
            return false
        }
        sharedPref.notificationGateCount += 1
        return true
    }
}
