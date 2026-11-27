package com.wavez.admob.interstitial_ads

import android.app.Activity
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition

interface AdInterstitialManager {

    val dismissFullScreenLastTime: Long

    fun isAdReady(): Boolean

    fun isAdEligibleToShow(): Boolean

    fun loadAd()

    fun setAdUnitId(adUnitId: List<String>)

    fun showAdFullScreen(activity: Activity, onAdComplete: () -> Unit)

    fun forceShowAdFullScreen(activity: Activity, onAdComplete: () -> Unit)

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)
}