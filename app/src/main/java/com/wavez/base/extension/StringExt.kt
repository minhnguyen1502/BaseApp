package com.wavez.base.extension

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.wavez.base.R
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

fun String.normalText(): String {
    val nfdNormalizedString: String = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfdNormalizedString).replaceAll("").uppercase(Locale.getDefault()).trim()
}

fun getSpanName(name: String, strSearch: String, color: Int): SpannableString {
    val spanName = SpannableString(name)
    val fileNameSub = name.normalText().lowercase()
    var str = strSearch.normalText().lowercase()
    if (str != "") {
        try {
            val pattern = str.toRegex()
            val found = pattern.findAll(fileNameSub)
            found.forEach {
                spanName.setSpan(
                    ForegroundColorSpan(color), it.range.first, it.range.last + 1, 0
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    return spanName
}

fun String.startsWithAnyIgnoreCase(prefixes: List<String>): Boolean {
    prefixes.forEach { prefix ->
        if (startsWith(prefix, true)) {
            return true
        }
    }
    return false
}

fun convertPhoneNumber(phoneNumber: String): String {
    return if (phoneNumber.isEmpty()) "" else try {
        PhoneNumberUtils.formatNumber(
            phoneNumber, Locale.getDefault().country
        )
    } catch (ex: Exception) {
        ""
    }
}

fun getShortMonthName(month: Int): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        } catch (e: Exception) {
            "Unknown"
        }
    } else {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
        }
        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    }
}

fun getSpanString(strHasColor: String, strNeedFind: String, color: Int): SpannableString {
    val spanName = SpannableString(strHasColor)
    val fileNameSub = strHasColor.normalText().lowercase()
    val str = strNeedFind.normalText().lowercase()
    if (str != "") {
        try {
            val pattern = str.toRegex()
            val found = pattern.findAll(fileNameSub)
            found.forEach {
                spanName.setSpan(
                    ForegroundColorSpan(color), it.range.first, it.range.last + 1, 0
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    return spanName
}

fun TextView.setLinearGradient(colors: IntArray) {
    val paint = paint
    val width = paint.measureText(text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width, textSize, colors, null, Shader.TileMode.REPEAT
    )

    paint.shader = textShader
}

fun removeInvalidSurrogates(input: String): String {
    val stringBuilder = StringBuilder()
    input.forEach { char ->
        if (Character.isHighSurrogate(char) || Character.isLowSurrogate(char)) {
            if (stringBuilder.isNotEmpty() && Character.isSurrogatePair(
                    stringBuilder.last(),
                    char
                )
            ) {
                stringBuilder.append(char)
            }
        } else {
            stringBuilder.append(char)
        }
    }
    return stringBuilder.toString()
}

fun Float.format2(): String = String.format(Locale.US, "%.2f", this)
fun Context.animateTextView(textView: TextView) {
    var dotCount = 0
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            textView.text = getString(R.string.loading) + ".".repeat(dotCount)
            dotCount = (dotCount + 1) % 4
            handler.postDelayed(this, 500L)
        }
    }
    handler.post(runnable)
}

fun TextView.startMarquee() {
    isSingleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    isFocusable = true
    isFocusableInTouchMode = true
}