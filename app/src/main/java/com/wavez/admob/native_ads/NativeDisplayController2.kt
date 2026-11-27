package com.wavez.admob.native_ads

import com.wavez.admob.native_ads.adapter.NativeAdPlaceholder2
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NativeDisplayController2(
    private val nativeAdPlaceholder: NativeAdPlaceholder2,
    private val nativeAdView: NativeAdView,
    // Kiểm tra lại ngay trước khi bind, vd ad vừa load xong cho biết đây là device test
    private val canShow: () -> Boolean = { true },
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var loadAdJob: Job? = null

    fun setupAdDisplay() {
        loadAdJob?.cancel()
        loadAdJob = null
        if (!nativeAdView.shouldShowAd || !canShow()) {
            nativeAdView.hidePlaceholder()
            return
        }
        val nativeAd = nativeAdPlaceholder.nativeAd
        if (nativeAd != null) {
            bind(nativeAd)
        } else {
            nativeAdView.showPlaceholder()
            loadAdJob = scope.launch {
                nativeAdPlaceholder.nativeAdFlow.collect { state ->
                    when (state) {
                        is NativeAdLoadState.Loaded -> {
                            val ad = nativeAdPlaceholder.nativeAd
                            if (ad != null) {
                                bind(ad)
                            } else {
                                nativeAdView.hidePlaceholder()
                            }
                            loadAdJob?.cancel()
                        }

                        is NativeAdLoadState.Loading -> {
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun bind(nativeAd: NativeAd) {
        loadAdJob?.cancel()
        loadAdJob = null
        if (!canShow()) {
            nativeAdView.hidePlaceholder()
            return
        }
        nativeAdView.showPlaceholder()
        nativeAdView.bind(nativeAd)
    }

    fun cleanupAdDisplay() {
        loadAdJob?.cancel()
        loadAdJob = null
        nativeAdPlaceholder.release()
    }

}