package com.wavez.base.extension

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

const val timeFormat1 = "dd-MM-yyyy"
const val timeFormat2 = "HH:mm aa"
const val timeFormat3 = "dd-MM-yyyy HH:mm"
const val timeFormat4 = "dd-MM"
const val timeFormat5 = "yyyy"
const val timeFormat6 = "dd/MM/yyyy"
const val timeFormat7 = "dd-MM-yyyy-HH-mm-ss"
const val timeFormat8 = "HH:mm:ss dd-MM-yyyy"
const val timeFormat9 = "dd/MM/yyyy - HH:mm"
const val timeFormat10 = "yyyy:MM:dd HH:mm:ss"
const val timeFormat11 = "EEE, d MMM yyyy"
const val timeFormat12 = "EEE, d MMM"
const val timeFormat13 = "HH:mm"
const val timeFormat14 = "hh:mm a"

fun formatDateLocaleDMY(date: Long): String? {
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    return dateFormat.format(date)
}

fun formatSmartDate(stringToday: String, stringYesterday: String, timestamp: Long): String {
    val today = Calendar.getInstance()
    val inputDate = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    val sameYear = today.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)

    val isToday =
        today.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == inputDate.get(
            Calendar.DAY_OF_YEAR
        )

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday =
        yesterday.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == inputDate.get(
            Calendar.DAY_OF_YEAR
        )

    return when {
        isToday -> stringToday
        isYesterday -> stringYesterday
        sameYear -> {
            val sdf = SimpleDateFormat(timeFormat12, Locale.ENGLISH)
            sdf.format(Date(timestamp))
        }

        else -> {
            val sdf = SimpleDateFormat(timeFormat11, Locale.ENGLISH)
            sdf.format(Date(timestamp))
        }
    }
}

fun formatTimeLocale(date: Long): String? {
    return formatTimeLocaleFormat(date)
}

fun formatTimeLocaleFormat(date: Long): String? {
    val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    return dateFormat.format(date)
}

fun getDateTimeOfWeekFormat(
    milliSeconds: Long, timeFormat: String = "EE, dd-MM-yyyy, HH:mm aa"
): String {
    val formatter = SimpleDateFormat(timeFormat, Locale.getDefault())
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun getDateTimeFormat(milliSeconds: Long, timeFormat: String = "HH:mm dd-MM-yyyy"): String {
    val formatter = SimpleDateFormat(timeFormat, Locale.ENGLISH)
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun getDateFormat(milliSeconds: Long, timeFormat: String = "dd.MM.yyyy HH:mm"): String {
    val formatter = SimpleDateFormat(timeFormat, Locale.ENGLISH)
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun formatDateLocale(date: Long): String? {
    val dateFormat =
        DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault())
    return dateFormat.format(date)
}

fun formatDateLocaleMY(date: Long): String? {
    val today = Calendar.getInstance()
    val inputDate = Calendar.getInstance().apply {
        timeInMillis = date
    }

    val sameYear = today.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)

    return when {
        sameYear -> {
            val sdf = SimpleDateFormat("MMMM", Locale.ENGLISH)
            sdf.format(Date(date))
        }

        else -> {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            sdf.format(Date(date))
        }
    }
}

fun String.toMillisecond(
    format: String = timeFormat8, locale: Locale = Locale.getDefault()
): Long? = SimpleDateFormat(format, locale).parse(this)?.time

fun Long.toFormattedTime(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun Long.formatTime(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("dd MMM ‘yy - HH:mm", Locale.getDefault())
    return formatter.format(date).uppercase(Locale.getDefault())
}

fun Long.formatDate(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    return formatter.format(date).uppercase(Locale.getDefault())
}

fun formatTimeString(epochMillis: Long, is24h: Boolean): String {
    val pattern = if (is24h) timeFormat13 else timeFormat14
    val date = Date(epochMillis)
    val formatter = SimpleDateFormat(pattern, Locale.UK)
    return formatter.format(date).uppercase(Locale.UK)
}

fun formatDateString(epochMillis: Long): String {
    val date = Date(epochMillis)
    val formatter = SimpleDateFormat(timeFormat6, Locale.getDefault())
    return formatter.format(date)
}