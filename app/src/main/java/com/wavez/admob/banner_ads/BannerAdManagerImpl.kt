package com.wavez.admob.banner_ads

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.base.storage.SharedPref
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Loads a banner ad with the specified ad unit ID. If adIdHigh is provided and fails,
 * it falls back to adId. Supports collapsible ads with optional load and show conditions.
 *
 * @param adId The default ad unit ID.
 * @param adIdHigh The high-priority ad unit ID (optional).
 * @param adSize The size of the ad.
 * @param collapsible Whether the ad is collapsible.
 * @param onAdLoaded Callback invoked when the ad is successfully loaded.
 * @param onAdFailedToLoad Callback invoked when the ad fails to load.
 */

class BannerAdManagerImpl(
    private val context: Context,
    private val sharedPref: SharedPref
) : BannerAdManager {

    private var adView: AdView? = null
    private var isLoading = false
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

    private val handler = Handler(Looper.getMainLooper())

    private var timeoutRunnable: Runnable? = null

    private var isHandled = false
    private var loadRequestToken = 0

    companion object {
        private const val LOAD_TIMEOUT_MS = 8_000L
    }

    private fun isAdEligibleToLoad(): Boolean {
        if (!sharedPref.canRequestAds) return false
        if (isLoading) return false
        val additionalLoadCondition = additionalLoadCondition
        if (additionalLoadCondition != null && !additionalLoadCondition.shouldLoad()) {
            return false
        }
        val additionalShowCondition = additionalShowCondition
        if (additionalShowCondition != null) {
            return additionalShowCondition.shouldShow()
        }

        return true
    }

    override fun loadBannerAdHigh(
        adId: String,
        adIdHigh: String?,
        adSize: AdSizeBanner,
        collapsible: Boolean,
        onAdLoaded: ((AdView) -> Unit)?,
        onAdFailedToLoad: (() -> Unit)?
    ) {
        val adUnitId = adIdHigh.takeIf { !it.isNullOrEmpty() } ?: adId
        Log.d("Log_Dev_KenTo", "loadBannerAd: " + isAdEligibleToLoad())
        if (!isAdEligibleToLoad()) {
            onAdFailedToLoad?.invoke()
            return
        }
        if (isLoading) {
            return
        }
        isLoading = true
        isHandled = false
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("BANNER_HIGH", adUnitId, requestToken, "hasHigh=${!adIdHigh.isNullOrEmpty()}")
        val adView = AdView(context)
        try {
            if (adSize == AdSizeBanner.FULL_BANNER) {
                adView.setAdSize(getAdaptiveSize())
            } else {
                adView.setAdSize(adSize.toAdSize())
            }
        } catch (e: Exception) {
            adView.setAdSize(adSize.toAdSize())
        }

        adView.adUnitId = adUnitId
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
//                isLoading = false
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER_HIGH", adUnitId, requestToken, loadRequestToken, "onAdLoaded")
                    return
                }
                AdTimeoutLogger.loadSuccess("BANNER_HIGH", adUnitId, requestToken)
                finishLoadRequest()
                AdRevenueTracker.trackAdRevenue(adView)
                onAdLoaded?.invoke(adView)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
//                isLoading = false
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER_HIGH", adUnitId, requestToken, loadRequestToken, "onAdFailedToLoad")
                    return
                }
                AdTimeoutLogger.loadFailed("BANNER_HIGH", adUnitId, requestToken, adError.message)
                finishLoadRequest()
                this@BannerAdManagerImpl.adView = null
                Log.d("Log_Dev_KenTo", "adError: ${adError.message}")

                if (adIdHigh.isNullOrEmpty()) {
                    Log.d("Log_Dev_KenTo", "onAdFailedToLoad: ")
                    onAdFailedToLoad?.invoke()
                } else {
                    Log.d("Log_Dev_KenTo", "onAdFailedToLoad: loadBannerAd ")
                    AdTimeoutLogger.fallback("BANNER_HIGH", adUnitId, requestToken, "fallbackToDefault=$adId")
                    loadBannerAdHigh(adId, null, adSize, collapsible, onAdLoaded, onAdFailedToLoad)
                }
            }
        }
        this.adView = adView
        startLoadTimeout("BANNER_HIGH", adUnitId, requestToken) {
            this@BannerAdManagerImpl.adView = null
            if (adIdHigh.isNullOrEmpty()) {
                onAdFailedToLoad?.invoke()
            } else {
                AdTimeoutLogger.fallback("BANNER_HIGH", adUnitId, loadRequestToken, "fallbackToDefault=$adId")
                loadBannerAdHigh(adId, null, adSize, collapsible, onAdLoaded, onAdFailedToLoad)
            }
        }
        val adRequest = AdRequest.Builder()
        if (collapsible) {
            val extras = Bundle()
            extras.putString("collapsible", "bottom")
            adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        adView.loadAd(adRequest.build())
    }

    override fun loadBannerAdWithAdSize(
        viewContext: Context,
        adUnitId: String,
        adSize: AdSize,
        collapsible: Boolean,
        onAdLoaded: ((AdView) -> Unit)?,
        onAdFailedToLoad: (() -> Unit)?
    ) {
        Log.d("log_banner", "loadBannerAdWithAdSize: $adUnitId")
        if (!isAdEligibleToLoad()) {
            onAdFailedToLoad?.invoke()
            return
        }
        isLoading = true
        isHandled = false
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("BANNER_ADSIZE", adUnitId, requestToken)
        val adView = AdView(viewContext)
        adView.setAdSize(adSize)
        adView.adUnitId = adUnitId
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER_ADSIZE", adUnitId, requestToken, loadRequestToken, "onAdLoaded")
                    return
                }
                AdTimeoutLogger.loadSuccess("BANNER_ADSIZE", adUnitId, requestToken)
                finishLoadRequest()
                AdRevenueTracker.trackAdRevenue(adView)
                onAdLoaded?.invoke(adView)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER_ADSIZE", adUnitId, requestToken, loadRequestToken, "onAdFailedToLoad")
                    return
                }
                AdTimeoutLogger.loadFailed("BANNER_ADSIZE", adUnitId, requestToken, adError.message)
                finishLoadRequest()
                this@BannerAdManagerImpl.adView = null
                onAdFailedToLoad?.invoke()
            }
        }
        this.adView = adView
        startLoadTimeout("BANNER_ADSIZE", adUnitId, requestToken) {
            this@BannerAdManagerImpl.adView = null
            onAdFailedToLoad?.invoke()
        }
        val adRequest = AdRequest.Builder()
        if (collapsible) {
            val extras = Bundle()
            extras.putString("collapsible", "bottom")
            adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        adView.loadAd(adRequest.build())
    }

    override fun loadBannerAd(
        adUnitId: String,
        collapsible: Boolean,
        adSize: AdSizeBanner,
        onAdLoaded: ((AdView) -> Unit)?,
        onAdFailedToLoad: (() -> Unit)?
    ) {
        val adUnitId = adUnitId
        Log.d("log_banner", "loadBannerAd: $adUnitId")
        if (!isAdEligibleToLoad()) {
            onAdFailedToLoad?.invoke()
            return
        }
        isLoading = true
        isHandled = false
        val requestToken = ++loadRequestToken
        AdTimeoutLogger.loadStart("BANNER", adUnitId, requestToken)
        val adView = AdView(context)
        try {
            if (adSize == AdSizeBanner.FULL_BANNER) {
                adView.setAdSize(getAdaptiveSize())
            } else {
                adView.setAdSize(adSize.toAdSize())
            }
        } catch (e: Exception) {
            adView.setAdSize(adSize.toAdSize())
        }
        adView.adUnitId = adUnitId
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
//                isLoading = false
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER", adUnitId, requestToken, loadRequestToken, "onAdLoaded")
                    return
                }
                AdTimeoutLogger.loadSuccess("BANNER", adUnitId, requestToken)
                finishLoadRequest()
                AdRevenueTracker.trackAdRevenue(adView)
                onAdLoaded?.invoke(adView)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                if (!isCurrentRequest(requestToken) || isHandled) {
                    AdTimeoutLogger.lateCallbackIgnored("BANNER", adUnitId, requestToken, loadRequestToken, "onAdFailedToLoad")
                    return
                }
                AdTimeoutLogger.loadFailed("BANNER", adUnitId, requestToken, adError.message)
                Log.d("hehehehehe", "onAdFailedToLoad: " + adError.message)
                finishLoadRequest()
//                isLoading = false
                this@BannerAdManagerImpl.adView = null
                onAdFailedToLoad?.invoke()
            }
        }
        this.adView = adView
        startLoadTimeout("BANNER", adUnitId, requestToken) {
            this@BannerAdManagerImpl.adView = null
            onAdFailedToLoad?.invoke()
        }
        val adRequest = AdRequest.Builder()
        if (collapsible) {
            val extras = Bundle()
            extras.putString("collapsible", "bottom")
            adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        adView.loadAd(adRequest.build())
    }

    private fun getAdaptiveSize(): AdSize {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val widthPixels = outMetrics.widthPixels
        val density = outMetrics.density

        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    private fun startLoadTimeout(type: String, adUnitId: String?, requestToken: Int, onTimeout: () -> Unit) {
        timeoutRunnable?.let(handler::removeCallbacks)
        timeoutRunnable = Runnable {
            if (!isCurrentRequest(requestToken) || isHandled) return@Runnable
            AdTimeoutLogger.timeout(type, adUnitId, requestToken)
            Log.d("log_banner", "load timeout")
            loadRequestToken++
            finishLoadRequest()
            onTimeout()
        }
        handler.postDelayed(timeoutRunnable!!,
            LOAD_TIMEOUT_MS
        )
    }

    private fun finishLoadRequest() {
        isLoading = false
        isHandled = true
        timeoutRunnable?.let(handler::removeCallbacks)
        timeoutRunnable = null
    }

    private fun isCurrentRequest(requestToken: Int): Boolean {
        return loadRequestToken == requestToken
    }

    override fun destroyBannerAd() {
        timeoutRunnable?.let(handler::removeCallbacks)
        timeoutRunnable = null
        isLoading = false
        adView?.destroy()
        adView = null
    }

    override fun pauseBannerAd() {
        adView?.pause()
    }

    override fun resumeBannerAd() {
        adView?.resume()
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }
}
