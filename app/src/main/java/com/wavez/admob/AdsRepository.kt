package com.wavez.admob

object AdsRepository {

    var isAdShowFullScreen = false

    var lastTimeDismissInterstitial = 0L

    var lastTimeDismissOpenAds = 0L
    var isBackScreen = false
        get() {
            val temp = field
            field = false
            return temp
        }

    var isBlockAutoReloadAds = false
        get() {
            val temp = field
            field = false
            return temp
        }
    var isClickCollapseNativeWhenResume = false
        get() {
            val temp = field
            field = false
            return temp
        }
    fun onClickCollapseNative() {
        isClickCollapseNativeWhenResume = true
        isBlockAutoReloadAds = true
    }

    fun onShowCollapse() {
        isShowCollapse = true
    }

    fun onDismissCollapse() {
        isShowCollapse = false
    }

    var isShowCollapse = false
        private set

    fun onBackScreen() {
        isBackScreen = true
    }

    // Collapse-icon tap count per native-ad position (e.g. "native_home"), used to match the
    // ad_show click-pattern. Lives only in memory, so it naturally resets on app restart —
    // persists across ad reloads within the same process, which is what lets a pattern like
    // "24" actually fire on the 2nd/4th tap instead of resetting every time the ad reloads.
    private val collapseClickCounts = mutableMapOf<String, Int>()

    fun registerCollapseClick(positionKey: String): Int {
        val next = (collapseClickCounts[positionKey] ?: 0) + 1
        collapseClickCounts[positionKey] = next
        return next
    }
}
