package com.wavez.admob

import android.util.Log

object AdTimeoutLogger {
    private const val TAG = "AdsTimeout"

    fun loadStart(type: String, adUnitId: String?, token: Int, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "LOAD_START", adUnitId, token, extra))
    }

    fun loadSuccess(type: String, adUnitId: String?, token: Int, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "LOAD_SUCCESS", adUnitId, token, extra))
    }

    fun loadFailed(type: String, adUnitId: String?, token: Int, error: String?, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "LOAD_FAILED", adUnitId, token, listOfNotNull(error, extra).joinToString(" | ").ifEmpty { null }))
    }

    fun timeout(type: String, adUnitId: String?, token: Int, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "TIMEOUT", adUnitId, token, extra))
    }

    fun lateCallbackIgnored(type: String, adUnitId: String?, token: Int, currentToken: Int, callback: String) {
        Log.d(
            TAG,
            buildMessage(
                type = type,
                event = "LATE_CALLBACK_IGNORED",
                adUnitId = adUnitId,
                token = token,
                extra = "currentToken=$currentToken callback=$callback"
            )
        )
    }

    fun fallback(type: String, adUnitId: String?, token: Int, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "FALLBACK_NEXT_ID", adUnitId, token, extra))
    }

    fun complete(type: String, adUnitId: String?, token: Int, extra: String? = null) {
        Log.d(TAG, buildMessage(type, "LOAD_COMPLETE", adUnitId, token, extra))
    }

    private fun buildMessage(
        type: String,
        event: String,
        adUnitId: String?,
        token: Int,
        extra: String?
    ): String {
        val id = adUnitId ?: "null"
        val suffix = extra?.takeIf { it.isNotBlank() }?.let { " | $it" } ?: ""
        return "$type | $event | token=$token | id=$id$suffix"
    }
}
