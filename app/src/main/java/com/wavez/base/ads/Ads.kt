package com.wavez.base.ads

import android.content.Context
import com.wavez.base.BuildConfig
import com.wavez.base.storage.SharedPreferencesApp


// TODO: thay các ID "ca-app-pub-REPLACE/REPLACE" bằng ad unit ID thật của app mới (debug dùng ID test của Google)
object Ads {

    private const val BANNER_TEST_ADS = "ca-app-pub-3940256099942544/6300978111"

    private const val REWARD_TEST_ADS = "ca-app-pub-3940256099942544/5224354917"

    private const val POPUP_TEST_ADS = "ca-app-pub-3940256099942544/1033173712"

    private const val REWARD_INTER_TEST_ADS = "ca-app-pub-3940256099942544/5354046379"

    private const val AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/9257395921"

    private const val NATIVE_AD_ID_TEST = "ca-app-pub-3940256099942544/2247696110"

    fun getRewardedAdsID(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(REWARD_TEST_ADS)
        } else {
            listOf(
                "",
                ""
            )
        }
    }

    fun getOpenAppAds(): String {
        return if (BuildConfig.DEBUG) {
            AD_UNIT_ID_TEST
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getBannerAdsSplash(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getInterAdsSplash(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getListAdNativeLanguageAds1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getListAdNativeLanguageAds2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getListAdNativeOnboarding1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"

            )
        }
    }

    fun getListAdNativeOnboarding1Click(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getListAdNativeObdFull1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(
                NATIVE_AD_ID_TEST, NATIVE_AD_ID_TEST
            )
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getListAdNativeObdFull2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(
                NATIVE_AD_ID_TEST, NATIVE_AD_ID_TEST
            )
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getNativeHome(): String {
        return if (BuildConfig.DEBUG) {
            NATIVE_AD_ID_TEST
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getInterstitialAdsClick(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE",
                "ca-app-pub-REPLACE/REPLACE"
            )
        }
    }

    fun getBannerHome(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getBannerInApp(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getBannerInAppHigh(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getNativeInApp(): String {
        return if (BuildConfig.DEBUG) {
            NATIVE_AD_ID_TEST
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getNativeInAppHigh(): String {
        return if (BuildConfig.DEBUG) {
            NATIVE_AD_ID_TEST
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

    fun getNativeAdd(): String {
        return if (BuildConfig.DEBUG) {
            NATIVE_AD_ID_TEST
        } else {
            "ca-app-pub-REPLACE/REPLACE"
        }
    }

}