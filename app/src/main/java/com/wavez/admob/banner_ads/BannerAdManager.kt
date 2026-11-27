package com.wavez.admob.banner_ads

import android.content.Context
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

interface BannerAdManager {

    fun loadBannerAdWithAdSize(
        viewContext: Context,
        adUnitId: String,
        adSize: AdSize,
        collapsible: Boolean = false,
        onAdLoaded: ((AdView) -> Unit)? = null,
        onAdFailedToLoad: (() -> Unit)? = null
    )

    fun loadBannerAdHigh(
        adId: String,
        adIdHigh: String? = null,
        adSize: AdSizeBanner = AdSizeBanner.FULL_BANNER,
        collapsible: Boolean = false,
        onAdLoaded: ((AdView) -> Unit)? = null,
        onAdFailedToLoad: (() -> Unit)? = null
    )

    fun loadBannerAd(
        adUnitId: String,
        collapsible: Boolean = false,
        adSize: AdSizeBanner = AdSizeBanner.FULL_BANNER,
        onAdLoaded: ((AdView) -> Unit)? = null,
        onAdFailedToLoad: (() -> Unit)? = null
    )

    fun destroyBannerAd()

    fun resumeBannerAd()

    fun pauseBannerAd()

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)
}