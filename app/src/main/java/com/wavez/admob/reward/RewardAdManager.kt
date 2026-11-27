package com.wavez.admob.reward

import android.app.Activity

interface RewardAdManager {

    fun setAdUnitIds(idAds: List<String>)

    fun loadRewardAd()

    suspend fun show(activity: Activity, onStateChanged: (RewardAdShowState) -> Unit)

}