package com.wavez.admob.native_ads.adapter

import com.wavez.admob.native_ads.NativeAdLoadState
import com.wavez.admob.native_ads.NativeAdRepositoryHighFloor
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.StateFlow

class NativeAdPlaceholder2(
    private val nativeAdRepository: NativeAdRepositoryHighFloor,
    private val nativeAdPosition: NativeAdPosition
) {

    private var _nativeAd: NativeAd? = null
    val nativeAd: NativeAd?
        get() {
            if (_nativeAd == null) {
                _nativeAd = nativeAdRepository.getLoadedNativeAd(nativeAdPosition)
            }
            return _nativeAd
        }

    val nativeAdFlow: StateFlow<NativeAdLoadState>
        get() = nativeAdRepository.getNativeAdLoadFlow(nativeAdPosition)


    fun release() {
        _nativeAd?.destroy()
        _nativeAd = null
    }

}
