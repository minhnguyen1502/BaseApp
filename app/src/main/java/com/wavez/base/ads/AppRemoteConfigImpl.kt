package com.wavez.base.ads

import android.util.Log
import com.wavez.admob.AdsRepository.lastTimeDismissInterstitial
import com.wavez.admob.AdsRepository.lastTimeDismissOpenAds
import com.wavez.admob.ShowCondition
import com.wavez.admob.interstitial_ads.AdInterstitialManager
import com.wavez.base.BuildConfig
import com.wavez.base.ads.AppRemoteConfig.Companion.AD_RESUME
import com.wavez.base.ads.AppRemoteConfig.Companion.AD_SHOW
import com.wavez.base.ads.AppRemoteConfig.Companion.BANNER_PLAYING_ON_OFF
import com.wavez.base.ads.AppRemoteConfig.Companion.INTER_ADS_GAP_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.INTER_CLICK_ON_OFF
import com.wavez.base.ads.AppRemoteConfig.Companion.INTER_OPEN_ADS_GAP_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.OPEN_ADS_GAP_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.OPEN_RESUME_ON_OFF
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_HOME
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_IN_APP
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_SPLASH
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_INTER_SPLASH
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_ADD
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_1
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_2
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_LANGUAGE1
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_LANGUAGE2
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_HOME
import com.wavez.base.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_IN_APP
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_COLLAPSE_AD_GAP_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_RELOAD_DELAY_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_HOME_POSITION_CONFIG_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_INAPP_POSITION_CONFIG_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.SPLASH_MAX_WAIT_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.TEST_ADS
import com.wavez.base.storage.SharedPref
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRemoteConfigImpl(
    private val sharedPref: SharedPref,
    private val adInterstitialManager: AdInterstitialManager,
) : AppRemoteConfig {

    private val configDefaults: Map<String, Any> = mapOf(
        INTER_OPEN_ADS_GAP_KEY to 10_000L,
        OPEN_ADS_GAP_KEY to 10_000L,
        INTER_ADS_GAP_KEY to 30_000L,
        AD_SHOW to 10,
        TEST_ADS to true,
        OPEN_RESUME_ON_OFF to true,
        INTER_CLICK_ON_OFF to true,
        SHOULD_SHOW_NATIVE_AD_LANGUAGE1 to true,
        SHOULD_SHOW_NATIVE_AD_LANGUAGE2 to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1 to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK to true,
        SHOULD_SHOW_BANNER_SPLASH to true,
        SHOULD_SHOW_INTER_SPLASH to true,
        SHOULD_SHOW_NATIVE_AD_FULL_1 to true,
        SHOULD_SHOW_NATIVE_AD_FULL_2 to true,
        SHOULD_SHOW_BANNER_HOME to true,
        SHOULD_SHOW_NATIVE_HOME to true,
        SHOULD_SHOW_NATIVE_IN_APP to true,
        SHOULD_SHOW_BANNER_IN_APP to true,
        SHOULD_SHOW_NATIVE_ADD to true,
        BANNER_PLAYING_ON_OFF to true,
        AD_RESUME to false,
        NATIVE_COLLAPSE_AD_GAP_KEY to 15_000L,
        NATIVE_RELOAD_DELAY_KEY to 20_000L,
        NATIVE_HOME_POSITION_CONFIG_KEY to """{"enable":1,"reload":20,"ad_show":24}""",
        NATIVE_INAPP_POSITION_CONFIG_KEY to """{"enable":1,"reload":20,"ad_show":24}""",
        SPLASH_MAX_WAIT_KEY to 15_000L,
        )

    override fun fetchRemoteConfig(onComplete: () -> Unit) {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setDefaultsAsync(configDefaults)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CoroutineScope(Dispatchers.IO).launch {
                    val interOpenAdsGap = remoteConfig.getLong(INTER_OPEN_ADS_GAP_KEY)
                    val openAdsGap = remoteConfig.getLong(OPEN_ADS_GAP_KEY)
                    val interAdsGap = remoteConfig.getLong(INTER_ADS_GAP_KEY)

                    val interClickOnOff = remoteConfig.getBoolean(INTER_CLICK_ON_OFF)
                    val appOpenResumeOnOff = remoteConfig.getBoolean(OPEN_RESUME_ON_OFF)

                    val nativeLanguage1 = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE1)
                    val nativeLanguage2 = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE2)

                    val nativeOnboarding1 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1)
                    val nativeFull1 = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_1)
                    val nativeFull2 = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_2)

                    val interSplash = remoteConfig.getBoolean(SHOULD_SHOW_INTER_SPLASH)
                    val bannerSplash = remoteConfig.getBoolean(SHOULD_SHOW_BANNER_SPLASH)

                    val nativeOnbClick =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK)
                    val nativeAdd = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_ADD)

                    val bannerHome = remoteConfig.getBoolean(SHOULD_SHOW_BANNER_HOME)
                    val nativeHome = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_HOME)

                    val nativeInApp = remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_IN_APP)
                    val bannerInApp = remoteConfig.getBoolean(SHOULD_SHOW_BANNER_IN_APP)
                    val bannerPlaying = remoteConfig.getBoolean(BANNER_PLAYING_ON_OFF)
                    val adResume = remoteConfig.getBoolean(AD_RESUME)
                    val nativeCollapseAdGap = remoteConfig.getLong(NATIVE_COLLAPSE_AD_GAP_KEY)
                    val nativeReloadDelayMs = remoteConfig.getLong(NATIVE_RELOAD_DELAY_KEY)
                    val adShow = remoteConfig.getLong(AD_SHOW).toInt()
                    val testAds = remoteConfig.getBoolean(TEST_ADS)
                    val nativeHomePositionConfig = remoteConfig.getString(NATIVE_HOME_POSITION_CONFIG_KEY)
                    val nativeInAppPositionConfig = remoteConfig.getString(NATIVE_INAPP_POSITION_CONFIG_KEY)
                    val splashMaxWait = remoteConfig.getLong(SPLASH_MAX_WAIT_KEY)

                    // SET VALUE
                    sharedPref.interOpenAdsGap = interOpenAdsGap
                    sharedPref.openAdsGap = openAdsGap
                    sharedPref.interAdsGap = interAdsGap
                    sharedPref.interClickOnOff = interClickOnOff
                    sharedPref.appOpenResumeOnOff = appOpenResumeOnOff
                    sharedPref.nativeLanguage1OnOff = nativeLanguage1
                    sharedPref.nativeLanguage2OnOff = nativeLanguage2
                    sharedPref.nativeOnboarding1OnOff = nativeOnboarding1
                    sharedPref.nativeFull1OnOff = nativeFull1
                    sharedPref.nativeFull2OnOff = nativeFull2
                    sharedPref.interSplashOnOff = interSplash
                    sharedPref.bannerSplashOnOff = bannerSplash
                    sharedPref.nativeOnb1ClickOnOff = nativeOnbClick
                    sharedPref.bannerHomeOnOff = bannerHome
                    sharedPref.nativeHomeOnOff = nativeHome
                    sharedPref.nativeInAppOnOff = nativeInApp
                    sharedPref.bannerInAppOnOff = bannerInApp
                    sharedPref.bannerPlayingOnOff = bannerPlaying
                    sharedPref.nativeAddOnOff = nativeAdd
                    sharedPref.adResume = adResume
                    sharedPref.nativeCollapseAdGap = nativeCollapseAdGap
                    sharedPref.nativeReloadDelayMs = nativeReloadDelayMs
                    sharedPref.adShow = adShow
                    sharedPref.testAdsOnOff = testAds
                    sharedPref.setNativeAdPositionConfigJson(NATIVE_HOME_POSITION_CONFIG_KEY, nativeHomePositionConfig)
                    sharedPref.setNativeAdPositionConfigJson(NATIVE_INAPP_POSITION_CONFIG_KEY, nativeInAppPositionConfig)
                    sharedPref.splashMaxWaitMs = splashMaxWait

                    Log.d(
                        "minh_remote_config", """
                    interOpenAdsGap = $interOpenAdsGap
                    openAdsGap = $openAdsGap
                    interAdsGap = $interAdsGap
                    interClickOnOff = $interClickOnOff
                    appOpenResumeOnOff = $appOpenResumeOnOff
                    nativeLanguage1 = $nativeLanguage1
                    nativeLanguage2 = $nativeLanguage2
                    nativeOnboarding1 = $nativeOnboarding1
                    nativeFull1 = $nativeFull1
                    nativeFull2 = $nativeFull2
                    interSplash = $interSplash
                    bannerSplash = $bannerSplash
                    nativeOnbClick = $nativeOnbClick
                    bannerHome = $bannerHome
                    nativeHome = $nativeHome
                    nativeAdd = $nativeAdd
                    nativeInApp = $nativeInApp
                    bannerInApp = $bannerInApp
                    adShow = $adShow
                    testAds = $testAds
                    adResume = $adResume
                    nativeCollapseAdGap = $nativeCollapseAdGap
                    nativeReloadDelayMs = $nativeReloadDelayMs
                    nativeHomePositionConfig = $nativeHomePositionConfig
                    nativeInAppPositionConfig = $nativeInAppPositionConfig
                    splashMaxWait = $splashMaxWait
                """.trimIndent()
                    )
                    setupAds()
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }

    private fun setupAds() {
        setupAdInterstitialManager()
    }

    private fun setupAdInterstitialManager() {
        adInterstitialManager.setShowCondition(object : ShowCondition {
            override fun shouldShow(): Boolean {
                val isOverGap =
                    System.currentTimeMillis() - lastTimeDismissInterstitial >= sharedPref.interAdsGap
                Log.d(
                    "log_time",
                    "log_time: $lastTimeDismissInterstitial ---- ${sharedPref.interAdsGap} ----- $isOverGap"
                )
                val isOverGapWithInterAd =
                    System.currentTimeMillis() - lastTimeDismissOpenAds >= sharedPref.interOpenAdsGap
                return isOverGap && isOverGapWithInterAd && sharedPref.interClickOnOff
            }
        })
    }
}
