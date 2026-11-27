package com.wavez.admob.native_ads

sealed class NativeAdLoadState {
    data object Loading : NativeAdLoadState()
    data object Loaded : NativeAdLoadState()
    data object Failed : NativeAdLoadState()
}