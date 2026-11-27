package com.wavez.base.ads

import android.util.Log
import com.wavez.admob.AdsRepository
import com.wavez.base.storage.SharedPref
import javax.inject.Inject

class AdClickPercentTageManager @Inject constructor(
    private val sharedPref: SharedPref,
) {

    /**
     * Click-bait trigger for a native-collapse position: each tap on the collapse icon is
     * counted per [positionKey] (reset when a fresh native ad is bound there). The position's
     * `ad_show` remote-config value is matched digit-by-digit — e.g. 24 fires on the 2nd and
     * 4th tap, 0 (default) never fires.
     */
    fun shouldShowAd(positionKey: String): Boolean {
        val config = sharedPref.getNativeAdPositionConfig(positionKey)
        if (!config.enable) {
            Log.d("minh_adShow", "positionKey=$positionKey, disabled by config.enable=false")
            return false
        }
        val clickCount = AdsRepository.registerCollapseClick(positionKey)
        val shouldShow = config.matchesClickCount(clickCount)

        Log.d(
            "minh_adShow",
            "positionKey=$positionKey, adShowPattern=${config.adShowPattern}, " +
                    "clickCount=$clickCount, shouldShow=$shouldShow"
        )

        return shouldShow
    }

}
