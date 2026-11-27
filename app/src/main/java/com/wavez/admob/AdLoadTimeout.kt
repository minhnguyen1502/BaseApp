package com.wavez.admob

import android.os.Handler
import android.os.Looper

class AdLoadTimeout(
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
    companion object {
        const val DEFAULT_TIMEOUT_MS = 8_000L
        const val NATIVE_TIMEOUT_MS = 4_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    fun start(onTimeout: () -> Unit) {
        cancel()
        timeoutRunnable = Runnable {
            timeoutRunnable = null
            onTimeout()
        }
        handler.postDelayed(timeoutRunnable!!, timeoutMs)
    }

    fun cancel() {
        timeoutRunnable?.let(handler::removeCallbacks)
        timeoutRunnable = null
    }
}
