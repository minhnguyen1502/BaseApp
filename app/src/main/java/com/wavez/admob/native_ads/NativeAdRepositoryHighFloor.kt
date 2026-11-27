package com.wavez.admob.native_ads

import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.StateFlow

interface NativeAdRepositoryHighFloor {

    val nativeAdLoadState: StateFlow<NativeAdLoadState>

    var preloadCount: Int

    fun loadAd(position: NativeAdPosition)

    fun setNativeAdIds(position: NativeAdPosition, ids: List<String>)

    fun getLoadedNativeAd(position: NativeAdPosition): NativeAd?

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)

    fun getNativeAdLoadFlow(position: NativeAdPosition): StateFlow<NativeAdLoadState>

    fun consumeAd(position: NativeAdPosition)

    var onAdClickedCallback: ((position: NativeAdPosition) -> Unit)?

}