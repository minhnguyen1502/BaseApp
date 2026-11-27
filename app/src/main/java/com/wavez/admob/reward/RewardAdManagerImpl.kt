package com.wavez.admob.reward

import android.app.Activity
import android.content.Context
import android.util.Log
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdsRepository
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class RewardAdManagerImpl(
    private val context: Context, private val sharedPref: SharedPref
) : RewardAdManager {

    private var rewardedAd: RewardedAd? = null
    private val adLoadState = MutableStateFlow(RewardLoadState.Initial)
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    private var adUnitIds: List<String> = emptyList()
    private var currentAdIndex = 0
    private var isShowAdProgress = false

    override fun setAdUnitIds(idAds: List<String>) {
        adUnitIds = idAds
    }

    override fun loadRewardAd() {
        if (!sharedPref.canRequestAds) return
        if (rewardedAd != null) return
        if (adLoadState.value != RewardLoadState.Initial) return
        if (adUnitIds.isEmpty()) throw Exception("Ad unit ids must be set")

        loadAdAtIndex(currentAdIndex)
    }

    private fun loadAdAtIndex(index: Int) {
        if (index >= adUnitIds.size) {
            adLoadState.tryEmit(RewardLoadState.Failed)
            return
        }

        adLoadState.tryEmit(RewardLoadState.Loading)
        val adRequest = AdRequest.Builder().build()
        val adUnitId = adUnitIds[index]

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("RewardAdManager", "Ad failed to load for $adUnitId, trying next")
                currentAdIndex++
                loadAdAtIndex(currentAdIndex) // fallback sang ID kế tiếp
            }

            override fun onAdLoaded(ad: RewardedAd) {
                AdRevenueTracker.trackAdRevenue(ad)
                rewardedAd = ad
                adLoadState.tryEmit(RewardLoadState.Loaded)
            }
        })
    }

    override suspend fun show(activity: Activity, onStateChanged: (RewardAdShowState) -> Unit) {
        if (isShowAdProgress) return
        isShowAdProgress = true

        withContext(Dispatchers.Main) {
            adLoadState.collect { state ->
                when (state) {
                    RewardLoadState.Initial -> onStateChanged(RewardAdShowState.Initial)
                    RewardLoadState.Loading -> onStateChanged(RewardAdShowState.Loading)
                    RewardLoadState.Loaded -> {
                        val ad = rewardedAd ?: throw IllegalStateException("Reward ad required")
                        show(ad, onStateChanged, activity)
                    }

                    RewardLoadState.Failed -> {
                        onStateChanged(RewardAdShowState.Failed("Reward failed to load"))
                        finishShowAdProgress()
                    }
                }
            }
        }
    }

    private fun CoroutineScope.show(
        rewardedAd: RewardedAd, onStateChanged: (RewardAdShowState) -> Unit, activity: Activity
    ) {
        var isEarned = false

        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                onStateChanged(RewardAdShowState.Clicked)
            }

            override fun onAdDismissedFullScreenContent() {
                AdsRepository.isAdShowFullScreen = false
                onStateChanged(RewardAdShowState.Completed(isEarned))
                finishShowAdProgress()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onStateChanged(RewardAdShowState.Failed(adError.message))
                finishShowAdProgress()
            }

            override fun onAdImpression() {
                onStateChanged(RewardAdShowState.Impression)
            }

            override fun onAdShowedFullScreenContent() {
                AdsRepository.isAdShowFullScreen = true
                onStateChanged(RewardAdShowState.Showing)
            }
        }

        rewardedAd.show(activity) {
            isEarned = true
            onStateChanged(RewardAdShowState.RewardEarned)
        }
    }

    private fun CoroutineScope.finishShowAdProgress() {
        cancel()
        isShowAdProgress = false
        resetRewardAd()
        currentAdIndex = 0
    }

    private fun resetRewardAd() {
        rewardedAd = null
        adLoadState.value = RewardLoadState.Initial
    }
}
