package com.wavez.base.extension

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

fun Long.kbToMbFormatted(): String {
    val sizeInMb = this / (1024.0 * 1024.0)
    val sizeInKb = this / 1024.0

    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
        decimalSeparator = '.'
    }
    val formatter = DecimalFormat("#.#", symbols)

    return if (sizeInMb >= 1) {
        "${formatter.format(sizeInMb)} MB"
    } else {
        "${formatter.format(sizeInKb)} KB"
    }
}

fun Int.getDurationDisplayFromMillis(): String {
    val ss = (this / 1000) % 60
    val mm = ((this / (1000 * 60)) % 60)
    val hours = ((this / (1000 * 60 * 60)) % 24)
    return if (hours != 0)
        String.format("%02d:%02d:%02d", hours, mm, ss)
    else
        String.format("%02d:%02d", mm, ss)
}

fun Int.getDurationDisplayFromOneOverTenSecond(): String {
    val rear = this % 10
    val second = this / 10
    val realSecond = second % 60
    val minute = second / 60
    return if (minute < 10) {
        String.format("%02d:%02d.%d", minute, realSecond, rear)
    } else String.format("%d:%02d.%d", minute, realSecond, rear)
}

fun Int.getDurationDisplay2FromMillis(): String {
    val mS = this / 100
    val rear = mS % 10
    val second = mS / 10
    val realSecond = second % 60
    val minute = second / 60
    return if (minute < 10) {
        String.format("%02d:%02d.%d", minute, realSecond, rear)
    } else String.format("%d:%02d.%d", minute, realSecond, rear)
}

fun Int.percentReverseToValue(totalValue: Int): Int {
    return ((totalValue - ((totalValue).toFloat() / (100).toFloat() * this)).roundToInt())
}

fun Int.reverseValueToPercent(totalValue: Int): Int {
    return (((totalValue - this) * 100) / totalValue).toFloat().toInt()
}

fun Int.percentToValue(totalValue: Int): Int {
    return ((((totalValue).toFloat() / (100).toFloat() * this)).roundToInt())
}

fun Int.valueToPercent(totalValue: Int): Int {
    return ((this * 100) / totalValue).toFloat().toInt()
}