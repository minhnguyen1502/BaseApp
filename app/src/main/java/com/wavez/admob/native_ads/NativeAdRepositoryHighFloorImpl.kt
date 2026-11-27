package com.wavez.admob.native_ads

import android.content.Context
import android.util.Log
import com.wavez.admob.AdLoadFailureDelay
import com.wavez.admob.AdLoadTimeout
import com.wavez.admob.AdRevenueTracker
import com.wavez.admob.AdTimeoutLogger
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.ads.AdTestDeviceDetector
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NativeAdRepositoryHighFloorImpl(
    private val context: Context,
    private val sharedPref: SharedPref
) : NativeAdRepositoryHighFloor {

    private val adUnitMap = mutableMapOf<NativeAdPosition, List<String>>()
    private val adIdIndexMap = mutableMapOf<NativeAdPosition, Int>()

    // Duy trì list native ads theo position
    private val nativeAdsMap = mutableMapOf<NativeAdPosition, MutableList<NativeAd>>()

    // Trạng thái load riêng cho từng position
    private val nativeAdLoadStateMap =
        mutableMapOf<NativeAdPosition, MutableStateFlow<NativeAdLoadState>>()

    // Quản lý loading status riêng biệt từng position
    private val isLoadingMap = mutableMapOf<NativeAdPosition, Boolean>()

    private val loadTimeoutMap = mutableMapOf<NativeAdPosition, AdLoadTimeout>()
    private val loadRequestTokenMap = mutableMapOf<NativeAdPosition, Int>()

    private val failedLoadGap = AdLoadFailureDelay()
    private val _nativeAdLoadState: MutableStateFlow<NativeAdLoadState> =
        MutableStateFlow(NativeAdLoadState.Loaded)

    override val nativeAdLoadState: StateFlow<NativeAdLoadState>
        get() = _nativeAdLoadState
    override var preloadCount: Int = 1
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

    private fun ensureStateFlow(position: NativeAdPosition): MutableStateFlow<NativeAdLoadState> {
        return nativeAdLoadStateMap.getOrPut(position) { MutableStateFlow(NativeAdLoadState.Loaded) }
    }

    private fun isLoading(position: NativeAdPosition): Boolean {
        return isLoadingMap[position] ?: false
    }

    private fun setLoading(position: NativeAdPosition, loading: Boolean) {
        isLoadingMap[position] = loading
    }

    private fun shouldLoad(position: NativeAdPosition): Boolean {
        if (isLoading(position)) {
            Log.d("native_debug", "Skip load: Already loading for $position")
            return false
        }
        if (!sharedPref.canRequestAds) {
            Log.d("native_debug", "Skip load: Can't request ads (consent) for $position")
            return false
        }
        val currentAds = nativeAdsMap[position]?.size ?: 0
        if (currentAds >= preloadCount) {
            Log.d(
                "native_debug",
                "Skip load: Already have $currentAds/$preloadCount ads for $position"
            )
            return false
        }
        if (additionalLoadCondition?.shouldLoad() == false) {
            Log.d("native_debug", "Skip load: Blocked by additionalLoadCondition for $position")
            return false
        }
        return true
    }

    override fun loadAd(position: NativeAdPosition) {
        Log.d("native_debug", "loadAd START: $position - ${System.currentTimeMillis()}")

        if (!adUnitMap.containsKey(position)) {
            Log.w("native_debug", "No Ad IDs set for $position, skip load")
            return
        }

        if (!shouldLoad(position)) {
            return
        }

        val nativeAdId = getCurrentAdUnitId(position)
        Log.d("native_debug", "Using Ad ID: $nativeAdId for $position")

        onLoading(position)

        val requestToken = nextLoadRequestToken(position)
        AdTimeoutLogger.loadStart("NATIVE_PRELOAD", nativeAdId, requestToken, "position=$position index=${adIdIndexMap[position] ?: 0}")
        startLoadTimeout(position, requestToken)

        val adLoader = AdLoader.Builder(context, nativeAdId).forNativeAd { ad ->
            if (!isCurrentLoadRequest(position, requestToken)) {
                AdTimeoutLogger.lateCallbackIgnored("NATIVE_PRELOAD", nativeAdId, requestToken, loadRequestTokenMap[position] ?: 0, "forNativeAd position=$position")
                ad.destroy()
                return@forNativeAd
            }
            AdTimeoutLogger.loadSuccess("NATIVE_PRELOAD", nativeAdId, requestToken, "position=$position")
            Log.d("native_debug", "Ad loaded SUCCESS for $position")

            adIdIndexMap[position] = 0
            AdRevenueTracker.trackAdRevenue(ad)
            AdTestDeviceDetector.update(ad, sharedPref)
            failedLoadGap.resetFailureCount()

            val ads = nativeAdsMap.getOrPut(position) { mutableListOf() }
            ads.add(ad)
            onLoaded(position)

            // Nếu chưa đủ preloadCount thì tiếp tục load thêm
            if (ads.size < preloadCount) {
                loadAd(position)
            } else {
                Log.d(
                    "native_debug", "Reached preloadCount: ${ads.size}/$preloadCount for $position"
                )
            }
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                if (!isCurrentLoadRequest(position, requestToken)) {
                    AdTimeoutLogger.lateCallbackIgnored("NATIVE_PRELOAD", nativeAdId, requestToken, loadRequestTokenMap[position] ?: 0, "onAdFailedToLoad position=$position")
                    return
                }
                AdTimeoutLogger.loadFailed("NATIVE_PRELOAD", nativeAdId, requestToken, error.message, "position=$position")
                Log.d("native_debug", "Ad failed for $position - Error: ${error.message}")

                val ids = adUnitMap[position] ?: return
                val currentIndex = adIdIndexMap[position] ?: 0

                if (currentIndex >= ids.lastIndex) {
                    adIdIndexMap[position] = 0
                    onFailed(position)
                    onLoaded(position)
                    Log.d("native_debug", "All ad IDs tried for $position, stop retry")
                    return
                }
                adIdIndexMap[position] = currentIndex + 1
                AdTimeoutLogger.fallback("NATIVE_PRELOAD", nativeAdId, requestToken, "position=$position nextIndex=${adIdIndexMap[position]}")
                onLoaded(position)
                loadAd(position)
                Log.d(
                    "native_debug",
                    "Retry with next Ad ID: index=${adIdIndexMap[position]} for $position"
                )
            }

            override fun onAdClicked() {
                super.onAdClicked()
                onAdClickedCallback?.invoke(position)
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun onLoaded(position: NativeAdPosition) {
        setLoading(position, false)
        loadTimeoutMap[position]?.cancel()
        ensureStateFlow(position).value = NativeAdLoadState.Loaded
    }

    private fun onFailed(position: NativeAdPosition) {
        setLoading(position, false)
        loadTimeoutMap[position]?.cancel()
        ensureStateFlow(position).value = NativeAdLoadState.Failed
    }

    private fun onLoading(position: NativeAdPosition) {
        setLoading(position, true)
        ensureStateFlow(position).value = NativeAdLoadState.Loading
    }

    private fun nextLoadRequestToken(position: NativeAdPosition): Int {
        val nextToken = (loadRequestTokenMap[position] ?: 0) + 1
        loadRequestTokenMap[position] = nextToken
        return nextToken
    }

    private fun isCurrentLoadRequest(position: NativeAdPosition, requestToken: Int): Boolean {
        return loadRequestTokenMap[position] == requestToken
    }

    private fun startLoadTimeout(position: NativeAdPosition, requestToken: Int) {
        val timeout = loadTimeoutMap.getOrPut(position) {
            AdLoadTimeout(AdLoadTimeout.NATIVE_TIMEOUT_MS)
        }
        timeout.start {
            if (!isCurrentLoadRequest(position, requestToken)) return@start
            AdTimeoutLogger.timeout("NATIVE_PRELOAD", getCurrentAdUnitId(position), requestToken, "position=$position index=${adIdIndexMap[position] ?: 0}")
            Log.d("native_debug", "Ad load timeout for $position")
            nextLoadRequestToken(position)

            val ids = adUnitMap[position] ?: run {
                onLoaded(position)
                return@start
            }
            val currentIndex = adIdIndexMap[position] ?: 0

            if (currentIndex >= ids.lastIndex) {
                adIdIndexMap[position] = 0
                AdTimeoutLogger.complete("NATIVE_PRELOAD", getCurrentAdUnitId(position), loadRequestTokenMap[position] ?: 0, "position=$position all ids tried")
                onLoaded(position)
                return@start
            }

            adIdIndexMap[position] = currentIndex + 1
            AdTimeoutLogger.fallback("NATIVE_PRELOAD", getCurrentAdUnitId(position), loadRequestTokenMap[position] ?: 0, "position=$position nextIndex=${adIdIndexMap[position]}")
            onLoaded(position)
            loadAd(position)
        }
    }


    override fun setNativeAdIds(position: NativeAdPosition, ids: List<String>) {
        adUnitMap[position] = ids
        adIdIndexMap[position] = 0
    }

    private fun getCurrentAdUnitId(position: NativeAdPosition): String {
        val ids = adUnitMap[position] ?: error("Ad IDs not set for $position")
        val index = adIdIndexMap[position] ?: 0
        return ids.getOrElse(index) { ids.first() }
    }

    override fun getLoadedNativeAd(position: NativeAdPosition): NativeAd? {
        val canShow = additionalShowCondition?.shouldShow() ?: true
        if (!canShow) return null

        val adList = nativeAdsMap[position]
        // Lấy native ad đầu tiên, KHÔNG xóa khỏi list để tránh mất ad trên observer
        return adList?.firstOrNull()
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }


    override fun getNativeAdLoadFlow(position: NativeAdPosition): StateFlow<NativeAdLoadState> {
        return ensureStateFlow(position)
    }

    override var onAdClickedCallback: ((position: NativeAdPosition) -> Unit)? = null

    override fun consumeAd(position: NativeAdPosition) {
        val canShow = additionalShowCondition?.shouldShow() ?: return
        if (!canShow) return

        val adList = nativeAdsMap[position] ?: return
        adList.removeFirstOrNull()
    }
}
