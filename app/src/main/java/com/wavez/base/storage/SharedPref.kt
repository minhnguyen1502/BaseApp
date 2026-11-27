package com.wavez.base.storage

import com.wavez.base.ads.NativeAdPositionConfig

interface SharedPref {
    var hasUserRated: Boolean
    var isAdmobTestDevice: Boolean

    /** Remote config test_ads: true = bật check chế độ dev / device test. */
    var testAdsOnOff: Boolean

    /** test_ads bật và thiết bị đang ở chế độ dev hoặc là AdMob test device. */
    val isTestAdsMode: Boolean
    var canRequestAds: Boolean

    var countRate: Int
    var bannerHomeOnOff: Boolean
    var nativeHomeOnOff: Boolean
    var nativeAddOnOff: Boolean
    var nativeInAppOnOff: Boolean
    var bannerInAppOnOff: Boolean
    var bannerPlayingOnOff: Boolean
    var appOpenResumeOnOff: Boolean
    var interClickOnOff: Boolean
    var adResume: Boolean
    var adShow : Int
    var nativeReloadDelayMs : Long

    var nativeFull2OnOff: Boolean
    var nativeFull1OnOff: Boolean
    var nativeOnb1ClickOnOff: Boolean
    var nativeOnboarding1OnOff: Boolean
    var nativeLanguage2OnOff: Boolean
    var nativeLanguage1OnOff: Boolean
    var isVip: Boolean
    var interOpenAdsGap: Long

    var nativeAdPreloadCount: Int

    var bannerSplashOnOff: Boolean

    var interSplashOnOff: Boolean

    var interAdsGap: Long

    var openAdsGap: Long

    var isNeedShowLanguage: Boolean

    var configLanguage: String
    var firstOpenApp: Boolean
    var countDeny: Int
    var isDevMode: Boolean

    var vipExpiryDate: Long

    var nativeCollapseAdGap: Long
    var splashMaxWaitMs: Long

    fun getNativeAdPositionConfig(positionKey: String): NativeAdPositionConfig
    fun setNativeAdPositionConfigJson(positionKey: String, json: String)
    var hasDecidedNotificationPermission: Boolean
    var hasRequestedNotificationPermission: Boolean
    var hasSeededReminderDefaults: Boolean

    var firstAppOpenTimestamp: Long
    var lastAppOpenTimestamp: Long
    var lastNoonReminderIndex: Int
    var lastEveningReminderIndex: Int

    var lastNotificationPermissionDialogShownAt: Long

    var notificationGateDate: String
    var notificationGateCount: Int

}