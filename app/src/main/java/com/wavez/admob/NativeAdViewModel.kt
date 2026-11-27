package com.wavez.admob

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.admob.native_ads.NativeAdLoadState
import com.wavez.admob.native_ads.NativeAdRepositoryHighFloor
import com.wavez.admob.native_ads.adapter.NativeAdPlaceholder2
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NativeAdViewModel @Inject constructor(
    private val nativeAdRepositoryHighFloor: NativeAdRepositoryHighFloor,
) : ViewModel() {

    private val nativeAdCache = mutableMapOf<NativeAdPosition, NativeAd?>()

    private val nativeAdMap = mutableMapOf<NativeAdPosition, MutableLiveData<NativeAd?>>()

    init {
        observeAllPositions()
        nativeAdRepositoryHighFloor.onAdClickedCallback = { position ->
            viewModelScope.launch {
                if (AdsRepository.isBlockAutoReloadAds) return@launch
                delay(1000)
                releaseAds(position)
                loadAd(position)
            }
        }
    }

    private fun observeAllPositions() {
        NativeAdPosition.entries.forEach { position ->
            nativeAdMap[position] = MutableLiveData(null)

            nativeAdRepositoryHighFloor.getNativeAdLoadFlow(position).onEach { state ->
                Log.d("ADS_DEBUG", "STATE = $position | $state")
                when (state) {
                    NativeAdLoadState.Loaded -> {
                        val newAd = nativeAdRepositoryHighFloor.getLoadedNativeAd(position)
                        Log.d("ADS_DEBUG", "LOADED AD = $newAd")

                        val cachedAd = nativeAdCache[position]
                        if (cachedAd != newAd) {
                            nativeAdCache[position] = newAd
                            nativeAdMap[position]?.postValue(newAd)
                            Log.d("ADS_DEBUG", "POST VALUE SUCCESS")
                        }
                    }

                    NativeAdLoadState.Failed -> {
                        Log.d("ADS_DEBUG", "Ad load failed for $position, hiding ad view")
                        nativeAdCache.remove(position)
                        nativeAdMap[position]?.postValue(null)
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
        }
    }


    fun setNativeAdIds(position: NativeAdPosition, adIds: List<String>) {
        nativeAdRepositoryHighFloor.setNativeAdIds(position, adIds)
    }


    fun loadAd(position: NativeAdPosition) {
        nativeAdRepositoryHighFloor.loadAd(position)
    }

    fun getNativeAdLiveData(position: NativeAdPosition): LiveData<NativeAd?> {
        return nativeAdMap[position] ?: MutableLiveData(null)
    }

    fun getNativeAdPlaceholder2(nativeAdPosition: NativeAdPosition): NativeAdPlaceholder2 {
        return NativeAdPlaceholder2(nativeAdRepositoryHighFloor, nativeAdPosition)
    }

    fun releaseAds(position: NativeAdPosition) {
        val nativeAd = getNativeAdPlaceholder2(position)
        nativeAdRepositoryHighFloor.consumeAd(position)
        nativeAd.release()
    }

}