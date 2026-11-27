package com.wavez.admob.interstitial_ads

import android.app.Activity

interface AdInterstitialSplashManager {

    fun isAdReady(): Boolean

    fun loadAd()

    fun showAdFullScreen(activity: Activity, onAdComplete: () -> Unit)

}