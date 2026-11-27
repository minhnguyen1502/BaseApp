package com.wavez.base.di

import android.content.Context
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.admob.banner_ads.BannerAdManager
import com.wavez.admob.banner_ads.BannerAdManagerImpl
import com.wavez.admob.interstitial_ads.AdInterstitialManager
import com.wavez.admob.interstitial_ads.AdInterstitialManagerImpl
import com.wavez.admob.interstitial_ads.AdInterstitialSplashManager
import com.wavez.admob.interstitial_ads.AdInterstitialSplashManagerImpl
import com.wavez.admob.native_ads.NativeAdRepositoryHighFloor
import com.wavez.admob.native_ads.NativeAdRepositoryHighFloorImpl
import com.wavez.admob.open_ads.OpenAdManager
import com.wavez.admob.open_ads.OpenAdManagerImpl
import com.wavez.admob.reward.RewardAdManager
import com.wavez.admob.reward.RewardAdManagerImpl
import com.wavez.base.ads.Ads
import com.wavez.base.ads.AppRemoteConfig
import com.wavez.base.ads.AppRemoteConfigImpl
import com.wavez.base.storage.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AdsModule {

    @Provides
    @Singleton
    fun provideShouldShowAdCondition(
        sharedPref: SharedPref
    ): ShowCondition {
        return object : ShowCondition {
            override fun shouldShow(): Boolean {
                val shouldShow = !sharedPref.isVip
                return shouldShow
            }
        }
    }

    @Provides
    @Singleton
    fun provideShouldLoadAdCondition(
        sharedPref: SharedPref
    ): LoadCondition {
        return object : LoadCondition {
            override fun shouldLoad(): Boolean {
                val shouldLoad = !sharedPref.isVip
                return shouldLoad
            }
        }
    }

    @Provides
    @Singleton
    fun provideNativeAdRepositoryMutil(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition,
        sharedPref: SharedPref
    ): NativeAdRepositoryHighFloor {
        return NativeAdRepositoryHighFloorImpl(context, sharedPref).apply {
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    fun provideBannerAdManager(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition,
        sharedPref: SharedPref
    ): BannerAdManager {
        return BannerAdManagerImpl(context, sharedPref).apply {
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    @Singleton
    fun provideAdInterstitialSplashManager(
        @ApplicationContext context: Context, sharedPref: SharedPref
    ): AdInterstitialSplashManager {
        return AdInterstitialSplashManagerImpl(
            context, Ads.getInterAdsSplash(), sharedPref
        )
    }

    @Provides
    @Singleton
    fun provideAdInterstitialManager(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition,
        sharedPref: SharedPref
    ): AdInterstitialManager {
        return AdInterstitialManagerImpl(context, sharedPref).apply {
            setAdUnitId(Ads.getInterstitialAdsClick())
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    @Singleton
    fun provideAdOpenManager(
        @ApplicationContext context: Context, sharedPref: SharedPref
    ): OpenAdManager {
        return OpenAdManagerImpl(
            context, sharedPref, Ads.getOpenAppAds()
        )
    }

    @Provides
    @Singleton
    fun provideRewardAdManager(
        @ApplicationContext context: Context, sharedPref: SharedPref
    ): RewardAdManager {
        return RewardAdManagerImpl(context, sharedPref).apply {
            setAdUnitIds(Ads.getRewardedAdsID())
        }
    }

    @Provides
    fun provideAppRemoteConfig(
        sharedPref: SharedPref,
        interstitialManager: AdInterstitialManager,
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            sharedPref, interstitialManager
        )
    }
}