package com.wavez.base.ads

interface  AppRemoteConfig {

    companion object {

        const val INTER_OPEN_ADS_GAP_KEY = "open_ad_inter_ad_gap"
        const val OPEN_ADS_GAP_KEY = "open_ad_gap"
        const val INTER_ADS_GAP_KEY = "inter_ad_gap"
        const val INTER_CLICK_ON_OFF = "inter_inapp_on_off"
        const val OPEN_RESUME_ON_OFF = "open_ads_on_off"

        const val AD_SHOW = "ad_show"
        const val TEST_ADS = "test_ads"
        const val SHOULD_SHOW_NATIVE_AD_LANGUAGE1 =
            "native_language_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_LANGUAGE2 =
            "native_language_2_on_off"
        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1 =
            "native_onboarding_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK =
            "native_onb1_click_on_off"
        const val SHOULD_SHOW_NATIVE_AD_FULL_1 = "native_full_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_FULL_2 = "native_full_2_on_off"
        const val SHOULD_SHOW_BANNER_SPLASH = "banner_splash_on_off"
        const val SHOULD_SHOW_INTER_SPLASH = "inter_splash_on_off"
        const val SHOULD_SHOW_BANNER_HOME = "banner_home_on_off"
        const val SHOULD_SHOW_NATIVE_HOME = "native_home_on_off"
        const val SHOULD_SHOW_NATIVE_IN_APP = "native_inapp_on_off"
        const val SHOULD_SHOW_NATIVE_ADD = "native_add_on_off"
        const val SHOULD_SHOW_BANNER_IN_APP = "banner_inapp_on_off"
        const val AD_RESUME  = "ad_resume"
        const val BANNER_PLAYING_ON_OFF  = "banner_playing_on_off"
        const val NATIVE_COLLAPSE_AD_GAP_KEY = "native_collapse_ad_gap"
        const val NATIVE_RELOAD_DELAY_KEY = "native_reload_delay"

        /** Per-placement native-collapse config: JSON {"enable":1,"reload":20,"ad_show":24}. */
        const val NATIVE_HOME_POSITION_CONFIG_KEY = "native_home"
        const val NATIVE_INAPP_POSITION_CONFIG_KEY = "native_inapp"

        const val SPLASH_MAX_WAIT_KEY = "splash_max_delay_time"

    }

    /** [onComplete] runs once fetched values (or defaults, on failure) are written to SharedPref. */
    fun fetchRemoteConfig(onComplete: () -> Unit = {})
}
