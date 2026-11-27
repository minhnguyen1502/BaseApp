package com.wavez.admob.native_ads

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.wavez.admob.AdLoadTimeout
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.base.ads.AdTestDeviceDetector
import com.wavez.base.ads.Ads
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import javax.inject.Inject

class NativeHelper @Inject constructor(
    private val activity: AppCompatActivity, private val sharePref: SharedPref
) {

    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(activity)
    private var loadRequestToken = 0

    fun load(
        shouldShowNativeAd: Boolean = true,
        nativeAdsId: String = Ads.getNativeHome(),
        onResponse: (NativeAd?) -> Unit
    ) = load(shouldShowNativeAd, listOf(nativeAdsId), onResponse)

    /**
     * Loads a native ad, trying [nativeAdsIds] in order and falling back to the next id on
     * failure/timeout — the same ad-unit-id waterfall (high floor first) that the onboarding
     * native preload flow uses in NativeAdRepositoryHighFloorImpl, applied here on-demand
     * instead of as a background preload.
     */
    fun load(
        shouldShowNativeAd: Boolean = true,
        nativeAdsIds: List<String>,
        onResponse: (NativeAd?) -> Unit
    ) {
        Log.d(
            "NativeHelper",
            "load() called | adIds=$nativeAdsIds canRequestAds=${googleMobileAdsConsentManager.canRequestAds} isVip=${sharePref.isVip} shouldShow=$shouldShowNativeAd"
        )

        if (!sharePref.canRequestAds) {
            Log.w("NativeHelper", "load() → NULL: canRequestAds = false")
            onResponse.invoke(null)
            return
        }

        if (sharePref.isVip) {
            Log.w("NativeHelper", "load() → NULL: isVip = true")
            onResponse.invoke(null)
            return
        }

        if (!shouldShowNativeAd) {
            Log.w("NativeHelper", "load() → NULL: shouldShowNativeAd = false")
            onResponse.invoke(null)
            return
        }

        if (nativeAdsIds.isEmpty()) {
            Log.w("NativeHelper", "load() → NULL: nativeAdsIds is empty")
            onResponse.invoke(null)
            return
        }

        loadWaterfall(nativeAdsIds, 0, onResponse)
    }

    private fun loadWaterfall(ids: List<String>, index: Int, onResponse: (NativeAd?) -> Unit) {
        val nativeAdsId = ids[index]
        var isHandled = false
        val requestToken = ++loadRequestToken
        val loadTimeout = AdLoadTimeout(AdLoadTimeout.NATIVE_TIMEOUT_MS)
        AdTimeoutLogger.loadStart(
            "NATIVE_HELPER", nativeAdsId, requestToken, "index=$index/${ids.lastIndex}"
        )

        fun fallbackOrRespond(reason: String) {
            if (index < ids.lastIndex) {
                AdTimeoutLogger.fallback(
                    "NATIVE_HELPER", nativeAdsId, requestToken, "nextIndex=${index + 1} reason=$reason"
                )
                loadWaterfall(ids, index + 1, onResponse)
            } else {
                AdTimeoutLogger.complete(
                    "NATIVE_HELPER", nativeAdsId, requestToken, "response=null reason=$reason"
                )
                onResponse.invoke(null)
            }
        }

        fun respond(ad: NativeAd?) {
            if (isHandled) {
                AdTimeoutLogger.lateCallbackIgnored(
                    "NATIVE_HELPER", nativeAdsId, requestToken, loadRequestToken, "respond"
                )
                ad?.destroy()
                return
            }
            isHandled = true
            loadTimeout.cancel()
            if (ad == null) {
                fallbackOrRespond("load_failed")
            } else {
                AdTimeoutLogger.loadSuccess("NATIVE_HELPER", nativeAdsId, requestToken)
                onResponse.invoke(ad)
            }
        }

        loadTimeout.start {
            if (isHandled) return@start
            isHandled = true
            AdTimeoutLogger.timeout("NATIVE_HELPER", nativeAdsId, requestToken, "response=null")
            fallbackOrRespond("timeout")
        }

        val adLoader = AdLoader.Builder(activity, nativeAdsId).forNativeAd { ad: NativeAd ->
            if (activity.isDestroyed || activity.isFinishing) {
                Log.w(
                    "NativeHelper",
                    "load() → NULL: activity isDestroyed=${activity.isDestroyed} isFinishing=${activity.isFinishing}"
                )
                ad.destroy()
                respond(null)
                return@forNativeAd
            }
            Log.d("NativeHelper", "load() → SUCCESS: ad loaded")
            AdRevenueTracker.trackAdRevenue(ad)
            AdTestDeviceDetector.update(ad, sharePref)
            respond(ad)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(
                    "NativeHelper",
                    "load() → NULL: onAdFailedToLoad | code=${adError.code} message=${adError.message} domain=${adError.domain}"
                )
                AdTimeoutLogger.loadFailed("NATIVE_HELPER", nativeAdsId, requestToken, adError.message)
                respond(null)
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

}
