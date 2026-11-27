package com.wavez.admob.open_ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.wavez.admob.AdLoadFailureDelay
import com.wavez.admob.AdLoadTimeout
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.AdsRepository
import com.wavez.admob.AdsRepository.lastTimeDismissInterstitial
import com.wavez.admob.AdsRepository.lastTimeDismissOpenAds
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OpenAdManagerImpl(
    private val context: Context, private val sharedPref: SharedPref, adUnitId: String
) : OpenAdManager {

    private var isLoading = false
    private var appOpenAd: AppOpenAd? = null
    private val loadFailureDelay = AdLoadFailureDelay()
    private var _adUnitId: String? = adUnitId
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    private val loadTimeout = AdLoadTimeout()
    private var loadRequestToken = 0
    private val _state = MutableStateFlow(OpenAdState.DISMISSED)
    override val state: StateFlow<OpenAdState>
        get() = _state

    private fun isAdEligibleToLoad(): Boolean {
        return !(isLoading || isAdReady() || loadFailureDelay.shouldDelayRetry() || !sharedPref.canRequestAds)
    }

    override fun loadAd() {
        val adUnitId = _adUnitId ?: throw Exception("Ad unit ID must be set")
        if (!isAdEligibleToLoad()) return
        isLoading = true
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("APP_OPEN", adUnitId, requestToken)
        startLoadTimeout(requestToken)
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(context, adUnitId, adRequest, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                if (requestToken != loadRequestToken) {
                    AdTimeoutLogger.lateCallbackIgnored("APP_OPEN", adUnitId, requestToken, loadRequestToken, "onAdLoaded")
                    return
                }
                AdTimeoutLogger.loadSuccess("APP_OPEN", adUnitId, requestToken)
                Log.d("log_test_123", "onAdLoaded: $ad")
                AdRevenueTracker.trackAdRevenue(ad)
                setLoadedAd(ad)
                loadFailureDelay.resetFailureCount()
                finishLoading()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (requestToken != loadRequestToken) {
                    AdTimeoutLogger.lateCallbackIgnored("APP_OPEN", adUnitId, requestToken, loadRequestToken, "onAdFailedToLoad")
                    return
                }
                AdTimeoutLogger.loadFailed("APP_OPEN", adUnitId, requestToken, loadAdError.message)
                Log.d("log_test_123", "onAdFailedToLoad cause: ${loadAdError.cause}")
                Log.d("log_test_123", "onAdFailedToLoad message: ${loadAdError.message}")
                loadFailureDelay.recordFailedLoad()
                finishLoading()
            }
        })
    }

    private fun finishLoading() {
        isLoading = false
        loadTimeout.cancel()
    }

    private fun startLoadTimeout(requestToken: Int) {
        loadTimeout.start {
            if (requestToken != loadRequestToken) return@start
            AdTimeoutLogger.timeout("APP_OPEN", _adUnitId, requestToken, "recordFailedLoad=true")
            Log.d("AppOpenAd", "load timeout")
            loadRequestToken++
            loadFailureDelay.recordFailedLoad()
            finishLoading()
        }
    }

    private fun setLoadedAd(ad: AppOpenAd) {
        appOpenAd = ad
    }

    override fun isAdReady(): Boolean {
        return appOpenAd != null
    }

    override fun show(activity: Activity, onContinueTask: (() -> Unit)?) {
        val currentTime = System.currentTimeMillis()

        val isOverGap = currentTime - lastTimeDismissOpenAds >= sharedPref.openAdsGap
        val isOverGapWithInterAd =
            currentTime - lastTimeDismissInterstitial >= sharedPref.interOpenAdsGap

        Log.d("AppOpenAd", "show() called")
        Log.d(
            "AppOpenAd",
            "isOverGap=$isOverGap, isOverGapWithInterAd=$isOverGapWithInterAd, resumeOnOff=${sharedPref.appOpenResumeOnOff}"
        )

        if (!isOverGap || !isOverGapWithInterAd || !sharedPref.appOpenResumeOnOff) {
            Log.d("AppOpenAd", "Skip show: condition not satisfied")
            onContinueTask?.invoke()
            return
        }

        if (appOpenAd == null) {
            Log.d("AppOpenAd", "Ad is null -> loadAd()")
            loadAd()
            onContinueTask?.invoke()
            return
        }

        if (AdsRepository.isAdShowFullScreen) {
            Log.d("AppOpenAd", "Skip show: another fullscreen ad is showing")
            onContinueTask?.invoke()
            return
        }

        val ad = appOpenAd
        Log.d("AppOpenAd", "Ad ready -> show")
        loadNewAd()

        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    Log.d("AppOpenAd", "onAdShowedFullScreenContent")
                    _state.value = OpenAdState.SHOWING
                    AdsRepository.isAdShowFullScreen = true
                    super.onAdShowedFullScreenContent()
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d("AppOpenAd", "onAdDismissedFullScreenContent")
                    _state.value = OpenAdState.DISMISSED
                    AdsRepository.isAdShowFullScreen = false
                    lastTimeDismissOpenAds = System.currentTimeMillis()
                    onContinueTask?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e("AppOpenAd", "onAdFailedToShowFullScreenContent: ${adError.message}")
                    _state.value = OpenAdState.DISMISSED
                    AdsRepository.isAdShowFullScreen = false // ⚠️ nên set false
                    onContinueTask?.invoke()
                }
            }

            ad.show(activity)
        } else {
            Log.d("AppOpenAd", "Ad null at show time")
            onContinueTask?.invoke()
        }
    }

    private fun loadNewAd() {
        appOpenAd = null
        loadAd()
    }

}


