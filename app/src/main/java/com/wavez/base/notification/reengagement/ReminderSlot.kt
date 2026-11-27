package com.wavez.base.notification.reengagement

import com.wavez.base.R

/**
 * One daily notification slot. Each slot owns its own AlarmManager request code so the
 * two slots can be armed/re-armed independently of one another.
 */
enum class ReminderSlot(
    val requestCode: Int,
    val hourOfDay: Int,
    val notificationId: Int,
    val titleBodyRes: List<Pair<Int, Int>>
) {
    NOON(
        requestCode = 5001,
        hourOfDay = 12,
        notificationId = 9001,
        titleBodyRes = listOf(
            R.string.reminder_noon_title_1 to R.string.reminder_noon_body_1,
            R.string.reminder_noon_title_2 to R.string.reminder_noon_body_2,
            R.string.reminder_noon_title_3 to R.string.reminder_noon_body_3,
        )
    ),
    EVENING(
        requestCode = 5002,
        hourOfDay = 19,
        notificationId = 9002,
        titleBodyRes = listOf(
            R.string.reminder_evening_title_1 to R.string.reminder_evening_body_1,
            R.string.reminder_evening_title_2 to R.string.reminder_evening_body_2,
            R.string.reminder_evening_title_3 to R.string.reminder_evening_body_3,
        )
    );

    companion object {
        const val EXTRA_SLOT_NAME = "EXTRA_SLOT_NAME"

        fun fromName(name: String?): ReminderSlot? = entries.find { it.name == name }
    }
}
