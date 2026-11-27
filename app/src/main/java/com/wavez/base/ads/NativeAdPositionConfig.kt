package com.wavez.base.ads

import org.json.JSONObject

/**
 * Per-placement native-collapse config, fetched as one JSON remote-config value per position
 * (e.g. key "native_home"): {"enable":1,"reload":20,"ad_show":24}.
 */
data class NativeAdPositionConfig(
    val enable: Boolean,
    val reloadSeconds: Int,
    val adShowPattern: Int,
) {
    companion object {
        val DEFAULT = NativeAdPositionConfig(enable = true, reloadSeconds = 0, adShowPattern = 0)

        fun fromJson(json: String?): NativeAdPositionConfig {
            if (json.isNullOrBlank()) return DEFAULT
            return try {
                val obj = JSONObject(json)
                NativeAdPositionConfig(
                    enable = obj.optInt("enable", 1) == 1,
                    reloadSeconds = obj.optInt("reload", 0),
                    adShowPattern = obj.optInt("ad_show", 0),
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }

    /**
     * [adShowPattern]'s digits are the click counts that trigger the click-bait navigation,
     * e.g. 24 means the 2nd and 4th tap on the collapse icon fire it; 0 never fires.
     */
    fun matchesClickCount(clickCount: Int): Boolean {
        if (adShowPattern <= 0) return false
        return adShowPattern.toString().any { it.digitToIntOrNull() == clickCount }
    }
}
