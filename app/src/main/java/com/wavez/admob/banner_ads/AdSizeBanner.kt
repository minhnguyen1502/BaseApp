package com.wavez.admob.banner_ads

import com.google.android.gms.ads.AdSize

enum class AdSizeBanner {
    BANNER, LARGE_BANNER, MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD
}

fun AdSizeBanner.toAdSize(): AdSize {
    return when (this) {
        AdSizeBanner.BANNER -> AdSize.BANNER
        AdSizeBanner.LARGE_BANNER -> AdSize.LARGE_BANNER
        AdSizeBanner.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
        AdSizeBanner.FULL_BANNER -> AdSize.FULL_BANNER
        AdSizeBanner.LEADERBOARD -> AdSize.LEADERBOARD
    }
}
