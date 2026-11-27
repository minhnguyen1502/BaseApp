package com.wavez.admob.open_ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface OpenAdManager {

    val state: StateFlow<OpenAdState>

    fun isAdReady(): Boolean

    fun loadAd()

    fun show(activity: Activity, onContinueTask: (() -> Unit)? = null)
}