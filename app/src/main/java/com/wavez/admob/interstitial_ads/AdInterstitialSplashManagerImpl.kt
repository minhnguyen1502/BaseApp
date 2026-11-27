package com.wavez.admob.interstitial_ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.wavez.admob.AdLoadFailureDelay
import com.wavez.admob.AdLoadTimeout
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.AdsRepository
import com.wavez.admob.AdsRepository.lastTimeDismissInterstitial
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdInterstitialSplashManagerImpl(
    private val context: Context,
    private val adUnitIds: List<String>,
    private val sharedPref: SharedPref
) : AdInterstitialSplashManager {

    private var _adUnitIds: List<String> = adUnitIds
    private var adIdIndex: Int = 0

    private val loadFailureDelay = AdLoadFailureDelay()
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    private val loadTimeout = AdLoadTimeout()
    private var loadRequestToken = 0

    override fun isAdReady(): Boolean {
        return interstitialAd != null
    }

    private fun isAdEligibleToShow(): Boolean {
        return interstitialAd != null
    }

    private fun isAdEligibleToLoad(): Boolean {
        if (sharedPref.isVip) return false
        if (!sharedPref.canRequestAds) return false
        if (isAdReady()) return false
        if (isLoading) return false
        return true
    }

    override fun loadAd() {
        if (!isAdEligibleToLoad()) return
        val adUnitId = getCurrentAdUnitId()
        Log.d("log_id_inter_splash", "loadAd: $adUnitId")
        isLoading = true
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("INTER_SPLASH", adUnitId, requestToken, "index=$adIdIndex")
        startLoadTimeout(requestToken)
        val adRequest = AdRequest.Builder().build()
        Log.d("log_id_inter_splash", "index: ${adIdIndex} ---  ${_adUnitIds.lastIndex}")

        InterstitialAd.load(
            context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    if (requestToken != loadRequestToken) {
                        AdTimeoutLogger.lateCallbackIgnored(
                            "INTER_SPLASH",
                            adUnitId,
                            requestToken,
                            loadRequestToken,
                            "onAdLoaded"
                        )
                        return
                    }
                    AdTimeoutLogger.loadSuccess(
                        "INTER_SPLASH",
                        adUnitId,
                        requestToken,
                        "index=$adIdIndex"
                    )
                    finishLoading()
                    interstitialAd = ad
                    AdRevenueTracker.trackAdRevenue(ad)
//                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    if (requestToken != loadRequestToken) {
                        AdTimeoutLogger.lateCallbackIgnored(
                            "INTER_SPLASH",
                            adUnitId,
                            requestToken,
                            loadRequestToken,
                            "onAdFailedToLoad"
                        )
                        return
                    }
                    AdTimeoutLogger.loadFailed(
                        "INTER_SPLASH",
                        adUnitId,
                        requestToken,
                        error.message,
                        "index=$adIdIndex"
                    )
                    finishLoading()
                    Log.d("log_id_inter_splash", "onAdFailedToLoad: ${error.message}")
                    if (adIdIndex >= _adUnitIds.lastIndex) {
//                        isLoading = false
                        return
                    }
                    adIdIndex++
//                    isLoading = false
                    AdTimeoutLogger.fallback(
                        "INTER_SPLASH",
                        adUnitId,
                        requestToken,
                        "nextIndex=$adIdIndex"
                    )
                    loadAd()
                }
            })
    }

    private fun startLoadTimeout(requestToken: Int) {
        loadTimeout.start {
            if (requestToken != loadRequestToken) return@start
            AdTimeoutLogger.timeout(
                "INTER_SPLASH",
                getCurrentAdUnitId(),
                requestToken,
                "index=$adIdIndex"
            )
            Log.d("log_id_inter_splash", "load timeout")
            loadRequestToken++
            finishLoading()
            if (adIdIndex < _adUnitIds.lastIndex) {
                adIdIndex++
                AdTimeoutLogger.fallback(
                    "INTER_SPLASH",
                    getCurrentAdUnitId(),
                    loadRequestToken,
                    "nextIndex=$adIdIndex"
                )
                loadAd()
            }
        }
    }

    private fun finishLoading() {
        isLoading = false
        loadTimeout.cancel()
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

        interstitialAd!!.let { ad ->
            interstitialAd = null
            setFullScreenContentCallback(ad, onAdComplete)
            ad.show(activity)
        }
    }

    private fun setFullScreenContentCallback(
        interstitialAd: InterstitialAd, onAdComplete: () -> Unit
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                AdsRepository.isAdShowFullScreen = true
                super.onAdShowedFullScreenContent()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                lastTimeDismissInterstitial = System.currentTimeMillis()
                AdsRepository.isAdShowFullScreen = false
                adIdIndex = 0
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                AdsRepository.isAdShowFullScreen = false
                onAdComplete()
            }
        }
    }
}
