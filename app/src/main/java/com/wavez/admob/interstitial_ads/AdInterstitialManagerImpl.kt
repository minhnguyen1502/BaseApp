package com.wavez.admob.interstitial_ads

import com.wavez.admob.AdLoadFailureDelay
import android.app.Activity
import android.content.Context
import android.util.Log
import com.wavez.admob.AdLoadTimeout
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.AdsRepository
import com.wavez.admob.AdsRepository.lastTimeDismissInterstitial
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.greenrobot.eventbus.EventBus

class AdInterstitialManagerImpl(
    private val context: Context,
    private val sharedPref: SharedPref
) : AdInterstitialManager {

    private var _adUnitIds: List<String> = emptyList()
    private var adIdIndex: Int = 0

    private val loadFailureDelay = AdLoadFailureDelay()
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    override var dismissFullScreenLastTime: Long = 0L
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    private val loadTimeout = AdLoadTimeout()
    private var loadRequestToken = 0

    override fun isAdReady(): Boolean {
        return interstitialAd != null
    }

    override fun isAdEligibleToShow(): Boolean {
        if (interstitialAd == null) {
            loadAd()
            return false
        }

        if (AdsRepository.isAdShowFullScreen) {
            return false
        }

        val additionalShowCondition = additionalShowCondition
        if (additionalShowCondition != null) {
            if (!additionalShowCondition.shouldShow()) return false
        }
        return true
    }

    private fun isAdEligibleToLoad(): Boolean {
        if (!sharedPref.canRequestAds) return false
        if (isAdReady()) return false
        if (isLoading) return false
//        if (loadFailureDelay.shouldDelayRetry()) return false
        val additionalLoadCondition = additionalLoadCondition
        if (additionalLoadCondition != null) {
            return additionalLoadCondition.shouldLoad()
        }
        return true
    }

    override fun loadAd() {
        if (!isAdEligibleToLoad()) return
        val adUnitId = getCurrentAdUnitId()
        Log.d("log_id_inter111111", "loadAd: $adUnitId")
        isLoading = true
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("INTERSTITIAL", adUnitId, requestToken, "index=$adIdIndex")
        startLoadTimeout(requestToken)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    if (requestToken != loadRequestToken) {
                        AdTimeoutLogger.lateCallbackIgnored("INTERSTITIAL", adUnitId, requestToken, loadRequestToken, "onAdLoaded")
                        return
                    }
                    AdTimeoutLogger.loadSuccess("INTERSTITIAL", adUnitId, requestToken, "index=$adIdIndex")
                    finishLoading()
                    interstitialAd = ad
                    AdRevenueTracker.trackAdRevenue(ad)
                    loadFailureDelay.resetFailureCount()
//                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    if (requestToken != loadRequestToken) {
                        AdTimeoutLogger.lateCallbackIgnored("INTERSTITIAL", adUnitId, requestToken, loadRequestToken, "onAdFailedToLoad")
                        return
                    }
                    AdTimeoutLogger.loadFailed("INTERSTITIAL", adUnitId, requestToken, error.message, "index=$adIdIndex")
                    finishLoading()
                    Log.d("log_id_inter", "onAdFailedToLoad: ${error.message}")
                    if (adIdIndex >= _adUnitIds.lastIndex) {
//                        isLoading = false
                        return
                    }
                    adIdIndex++
//                    isLoading = false
                    AdTimeoutLogger.fallback("INTERSTITIAL", adUnitId, requestToken, "nextIndex=$adIdIndex")
                    loadAd()
                }
            }
        )
    }

    private fun startLoadTimeout(requestToken: Int) {
        loadTimeout.start {
            if (requestToken != loadRequestToken) return@start
            AdTimeoutLogger.timeout("INTERSTITIAL", getCurrentAdUnitId(), requestToken, "index=$adIdIndex")
            Log.d("log_id_inter", "load timeout")
            loadRequestToken++
            finishLoading()
            if (adIdIndex < _adUnitIds.lastIndex) {
                adIdIndex++
                AdTimeoutLogger.fallback("INTERSTITIAL", getCurrentAdUnitId(), loadRequestToken, "nextIndex=$adIdIndex")
                loadAd()
            }
        }
    }

    private fun finishLoading() {
        isLoading = false
        loadTimeout.cancel()
    }

    override fun setAdUnitId(adUnitId: List<String>) {
        _adUnitIds = adUnitId
    }

    private fun getCurrentAdUnitId(): String {
        if (_adUnitIds.isEmpty()) {
            throw IllegalStateException("Interstitial ad ids are not set")
        }
        return _adUnitIds[adIdIndex]
    }

    override fun showAdFullScreen(activity: Activity, onAdComplete: () -> Unit) {
        if (!isAdEligibleToShow()) {
            onAdComplete()
            return
        }

        interstitialAd?.let { ad ->
            interstitialAd = null
            loadAd()
            setFullScreenContentCallback(ad, onAdComplete)
            ad.show(activity)
        } ?: run {
            loadAd()
            onAdComplete()
        }
    }

    override fun forceShowAdFullScreen(activity: Activity, onAdComplete: () -> Unit) {
        interstitialAd?.let { ad ->
            interstitialAd = null
            loadAd()
            setFullScreenContentCallback(ad, onAdComplete)
            ad.show(activity)
        } ?: onAdComplete()
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }

    private fun setFullScreenContentCallback(
        interstitialAd: InterstitialAd,
        onAdComplete: () -> Unit
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                AdsRepository.isAdShowFullScreen = true
//                EventBus.getDefault().post(InterstitialAdEvent(isShowing = true))
                super.onAdShowedFullScreenContent()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                AdsRepository.isAdShowFullScreen = false
                lastTimeDismissInterstitial = System.currentTimeMillis()
                adIdIndex = 0
//                EventBus.getDefault().post(InterstitialAdEvent(isShowing = false))
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                AdsRepository.isAdShowFullScreen = false
//                EventBus.getDefault().post(InterstitialAdEvent(isShowing = false))
                onAdComplete()
            }
        }
    }
}
