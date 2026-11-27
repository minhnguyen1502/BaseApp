package com.wavez.admob.native_ads

import com.google.android.gms.ads.nativead.NativeAd

interface NativeAdView {

    companion object {
        const val RELOAD_SHIMMER_MS = 300L
    }

    var shouldShowAd: Boolean

    var isBound: Boolean

    fun bind(nativeAd: NativeAd)

    fun showPlaceholder()

    fun hidePlaceholder()

}