package com.wavez.base.extension

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.Locale

fun convertTimeToHourMinuteSecond(time: Long): String {
    val secondTime = time / 1000
    val seconds = secondTime % 60
    val minutes = ((secondTime - seconds) / 60) % 60
    val hours = (secondTime - seconds - minutes * 60) / (60 * 60)
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

fun convertTimeToDate(strTime: String): String {
    try {
        val time = strTime.toLong()
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()
        ).format(time)
    } catch (_: Exception) {
        return ""
    }
}

fun roundTo6Decimals(value: Double): Double {
    return BigDecimal(value).setScale(6, RoundingMode.HALF_UP).toDouble()
}

