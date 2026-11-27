package com.wavez.base.storage

import android.content.Context
import android.content.SharedPreferences
import com.wavez.base.R
import com.wavez.base.ads.AppRemoteConfig.Companion.AD_RESUME
import com.wavez.base.ads.AppRemoteConfig.Companion.AD_SHOW
import com.wavez.base.ads.AppRemoteConfig.Companion.BANNER_PLAYING_ON_OFF
import com.wavez.base.ads.AppRemoteConfig.Companion.INTER_CLICK_ON_OFF
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_COLLAPSE_AD_GAP_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.NATIVE_RELOAD_DELAY_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.SPLASH_MAX_WAIT_KEY
import com.wavez.base.ads.AppRemoteConfig.Companion.OPEN_RESUME_ON_OFF
import com.wavez.base.ads.NativeAdPositionConfig
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
import com.wavez.base.ads.AppRemoteConfig.Companion.TEST_ADS

class SharedPreferencesApp(context: Context) : SharedPref {
    companion object {
        private const val PREFERENCE_FILE_KEY = "shared_preferences_app"

        private const val IS_VIP = "is_vip"

        private const val INTER_OPEN_ADS_GAP = "inter_open_ads_gap"

        private const val NATIVE_AD_PRELOAD_COUNT = "native_ad_preload_count"

        private const val INTER_ADS_GAP = "inter_ads_gap"

        private const val OPEN_ADS_GAP = "open_ads_gap"

        private const val IS_NEED_SHOW_LANGUAGE = "IS_NEED_SHOW_LANGUAGE"

        private const val LANGUAGE_CONFIG = "LANGUAGE_CONFIG"

        private const val URI_FROM_DEVICE = "URI_FROM_DEVICE"

        private const val PLAYER_START = "PLAYER_START"

        private const val ICON_SELECTED = "ICON_SELECTED"

        private const val COLOR_SELECTED = "COLOR_SELECTED"

        private const val IS_LINEAR = "IS_LINEAR"
        private const val IS_PIP = "IS_PIP"
        private const val IS_FIRST_OPEN_APP = "IS_FIRST_OPEN_APP"
        private const val HAS_SEEN_ADD_PLAYLIST_TUTORIAL = "HAS_SEEN_ADD_PLAYLIST_TUTORIAL"

        private const val SORT = "SORT"

        private const val HAS_USER_RATED = "HAS_USER_RATED"
        private const val IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"

        private const val COUNT_DENY = "COUNT_DENY"
        private const val VIP_EXPIRY_DATE = "VIP_EXPIRY_DATE"
        private const val IS_DEV_MODE = "IS_DEV_MODE"

        private const val FOLLOWED_MATCH_IDS = "FOLLOWED_MATCH_IDS"
        private const val HAS_DECIDED_NOTIFICATION_PERMISSION = "HAS_DECIDED_NOTIFICATION_PERMISSION"
        private const val HAS_REQUESTED_NOTIFICATION_PERMISSION = "HAS_REQUESTED_NOTIFICATION_PERMISSION"
        private const val HAS_SEEDED_REMINDER_DEFAULTS = "HAS_SEEDED_REMINDER_DEFAULTS"
        private const val SELECTED_XTREAM_PLAYLIST_ID = "SELECTED_XTREAM_PLAYLIST_ID"

        private const val FIRST_APP_OPEN_TIMESTAMP = "FIRST_APP_OPEN_TIMESTAMP"
        private const val LAST_APP_OPEN_TIMESTAMP = "LAST_APP_OPEN_TIMESTAMP"
        private const val LAST_NOON_REMINDER_INDEX = "LAST_NOON_REMINDER_INDEX"
        private const val LAST_EVENING_REMINDER_INDEX = "LAST_EVENING_REMINDER_INDEX"

        private const val HAS_ADDED_PLAYLIST = "HAS_ADDED_PLAYLIST"
        private const val HAS_PLAYED_CHANNEL = "HAS_PLAYED_CHANNEL"
        private const val PLAYLIST_READY_REMINDER_DUE_AT = "PLAYLIST_READY_REMINDER_DUE_AT"

        private const val LAST_NOTIFICATION_PERMISSION_DIALOG_SHOWN_AT = "LAST_NOTIFICATION_PERMISSION_DIALOG_SHOWN_AT"

        private const val NOTIFICATION_GATE_DATE = "NOTIFICATION_GATE_DATE"
        private const val NOTIFICATION_GATE_COUNT = "NOTIFICATION_GATE_COUNT"
        private const val IS_ADMOB_TEST_DEVICE = "IS_ADMOB_TEST_DEVICE"

        // Giá trị ads áp dụng khi thiết bị đang ở chế độ dev / device test (config test_ads = true)
        private const val TEST_MODE_AD_SHOW = 0
        private const val TEST_MODE_INTER_ADS_GAP = 90_000L
        private const val TEST_MODE_OPEN_ADS_GAP = 20_000L
        private const val TEST_MODE_INTER_OPEN_ADS_GAP = 20_000L
        private const val CAN_REQUEST_ADS = "can_request_ads"

    }

    override var hasUserRated: Boolean
        get() = sharedPref.getBoolean(HAS_USER_RATED, false)
        set(value) {
            editor.putBoolean(HAS_USER_RATED, value).apply()
        }

    override var isAdmobTestDevice: Boolean
        get() = sharedPref.getBoolean(IS_ADMOB_TEST_DEVICE, false)
        set(value) = editor.putBoolean(IS_ADMOB_TEST_DEVICE, value).apply()

    override var testAdsOnOff: Boolean
        get() = sharedPref.getBoolean(TEST_ADS, true)
        set(value) = editor.putBoolean(TEST_ADS, value).apply()

    override val isTestAdsMode: Boolean
        get() = testAdsOnOff && (isDevMode || isAdmobTestDevice)

    override var canRequestAds: Boolean
        get() = sharedPref.getBoolean(CAN_REQUEST_ADS, true)
        set(value) {
            editor.putBoolean(CAN_REQUEST_ADS, value).commit()
        }

    override var countRate: Int
        get() = sharedPref.getInt(IS_FIRST_LAUNCH, 0)
        set(value) {
            editor.putInt(IS_FIRST_LAUNCH, value).apply()
        }
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor
        get() = sharedPref.edit()


    override var isVip: Boolean
        get() = sharedPref.getBoolean(IS_VIP, false)
        set(value) {
            editor.putBoolean(IS_VIP, value).commit()
        }

    override var interOpenAdsGap: Long
        get() = if (isTestAdsMode) TEST_MODE_INTER_OPEN_ADS_GAP else sharedPref.getLong(INTER_OPEN_ADS_GAP, 10_000L)
        set(value) {
            editor.putLong(INTER_OPEN_ADS_GAP, value).commit()
        }

    override var nativeAdPreloadCount: Int
        get() = sharedPref.getInt(NATIVE_AD_PRELOAD_COUNT, 3)
        set(value) {
            editor.putInt(NATIVE_AD_PRELOAD_COUNT, value).commit()
        }

    override var bannerSplashOnOff: Boolean
        get() = sharedPref.getBoolean(SHOULD_SHOW_BANNER_SPLASH, true)
        set(value) {
            editor.putBoolean(SHOULD_SHOW_BANNER_SPLASH, value).apply()
        }

    override var interSplashOnOff: Boolean
        get() = sharedPref.getBoolean(SHOULD_SHOW_INTER_SPLASH, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_INTER_SPLASH, value).apply()

    override var nativeLanguage1OnOff: Boolean
        get() = sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE1, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE1, value).apply()

    override var nativeLanguage2OnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE2, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_LANGUAGE2, value).apply()

    override var nativeOnboarding1OnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1, value).apply()

    override var nativeOnb1ClickOnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK, value).apply()

    override var nativeFull1OnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_1, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_FULL_1, value).apply()

    override var nativeFull2OnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_2, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_AD_FULL_2, value).apply()

    override var interAdsGap: Long
        get() = if (isTestAdsMode) TEST_MODE_INTER_ADS_GAP else sharedPref.getLong(INTER_ADS_GAP, 30_000L)
        set(value) {
            editor.putLong(INTER_ADS_GAP, value).commit()
        }

    override var openAdsGap: Long
        get() = if (isTestAdsMode) TEST_MODE_OPEN_ADS_GAP else sharedPref.getLong(OPEN_ADS_GAP, 10_000L)
        set(value) {
            editor.putLong(OPEN_ADS_GAP, value).commit()
        }

    override var bannerHomeOnOff: Boolean
        get() = sharedPref.getBoolean(SHOULD_SHOW_BANNER_HOME, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_BANNER_HOME, value).apply()

    override var nativeHomeOnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_HOME, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_HOME, value).apply()

    override var nativeAddOnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_ADD, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_ADD, value).apply()

    override var nativeInAppOnOff: Boolean
        get() = !isTestAdsMode && sharedPref.getBoolean(SHOULD_SHOW_NATIVE_IN_APP, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_NATIVE_IN_APP, value).apply()

    override var bannerInAppOnOff: Boolean
        get() = sharedPref.getBoolean(SHOULD_SHOW_BANNER_IN_APP, true)
        set(value) = editor.putBoolean(SHOULD_SHOW_BANNER_IN_APP, value).apply()

    override var bannerPlayingOnOff: Boolean
        get() = sharedPref.getBoolean(BANNER_PLAYING_ON_OFF, true)
        set(value) = editor.putBoolean(BANNER_PLAYING_ON_OFF, value).apply()

    override var appOpenResumeOnOff: Boolean
        get() = sharedPref.getBoolean(OPEN_RESUME_ON_OFF, true)
        set(value) = editor.putBoolean(OPEN_RESUME_ON_OFF, value).apply()

    override var interClickOnOff: Boolean
        get() = sharedPref.getBoolean(INTER_CLICK_ON_OFF, true)
        set(value) = editor.putBoolean(INTER_CLICK_ON_OFF, value).apply()

    override var adResume: Boolean
        get() = sharedPref.getBoolean(AD_RESUME, false)
        set(value) = editor.putBoolean(AD_RESUME, value).apply()


    override var adShow: Int
        get() = if (isTestAdsMode) TEST_MODE_AD_SHOW else sharedPref.getInt(AD_SHOW, 10)
        set(value) = editor.putInt(AD_SHOW, value).apply()

    override var nativeReloadDelayMs: Long
        get() = sharedPref.getLong(NATIVE_RELOAD_DELAY_KEY, 20_000L)
        set(value) {
            editor.putLong(NATIVE_RELOAD_DELAY_KEY, value).commit()
        }

    override var isNeedShowLanguage: Boolean
        get() = sharedPref.getBoolean(IS_NEED_SHOW_LANGUAGE, true)
        set(value) {
            editor.putBoolean(IS_NEED_SHOW_LANGUAGE, value).commit()
        }

    override var configLanguage: String
        get() = sharedPref.getString(LANGUAGE_CONFIG, "") ?: ""
        set(value) {
            editor.putString(LANGUAGE_CONFIG, value).commit()
        }









    override var firstOpenApp: Boolean
        get() = sharedPref.getBoolean(IS_FIRST_OPEN_APP, true)
        set(value) {
            editor.putBoolean(IS_FIRST_OPEN_APP, value).commit()
        }


    override var countDeny: Int
        get() = sharedPref.getInt(COUNT_DENY, 0)
        set(value) {
            editor.putInt(COUNT_DENY, value).commit()
        }

    override var isDevMode: Boolean
        get() = sharedPref.getBoolean(IS_DEV_MODE, false)
        set(value) = editor.putBoolean(IS_DEV_MODE, value).apply()

    override var vipExpiryDate: Long
        get() = sharedPref.getLong(VIP_EXPIRY_DATE, 0L)
        set(value) {
            editor.putLong(VIP_EXPIRY_DATE, value).commit()
        }

    override var nativeCollapseAdGap: Long
        get() = sharedPref.getLong(NATIVE_COLLAPSE_AD_GAP_KEY, 15_000L)
        set(value) {
            editor.putLong(NATIVE_COLLAPSE_AD_GAP_KEY, value).commit()
        }

    // Hard-capped at 15s client-side regardless of the remote value (doc: "max 15s").
    override var splashMaxWaitMs: Long
        get() = sharedPref.getLong(SPLASH_MAX_WAIT_KEY, 15_000L).coerceAtMost(15_000L)
        set(value) {
            editor.putLong(SPLASH_MAX_WAIT_KEY, value).commit()
        }

    override fun getNativeAdPositionConfig(positionKey: String): NativeAdPositionConfig =
        NativeAdPositionConfig.fromJson(sharedPref.getString(positionKey, null))

    override fun setNativeAdPositionConfigJson(positionKey: String, json: String) {
        editor.putString(positionKey, json).apply()
    }


    override var hasDecidedNotificationPermission: Boolean
        get() = sharedPref.getBoolean(HAS_DECIDED_NOTIFICATION_PERMISSION, false)
        set(value) {
            editor.putBoolean(HAS_DECIDED_NOTIFICATION_PERMISSION, value).commit()
        }

    override var hasRequestedNotificationPermission: Boolean
        get() = sharedPref.getBoolean(HAS_REQUESTED_NOTIFICATION_PERMISSION, false)
        set(value) {
            editor.putBoolean(HAS_REQUESTED_NOTIFICATION_PERMISSION, value).commit()
        }
    override var hasSeededReminderDefaults: Boolean
        get() = sharedPref.getBoolean(HAS_SEEDED_REMINDER_DEFAULTS, false)
        set(value) {
            editor.putBoolean(HAS_SEEDED_REMINDER_DEFAULTS, value).commit()
        }


    override var firstAppOpenTimestamp: Long
        get() = sharedPref.getLong(FIRST_APP_OPEN_TIMESTAMP, 0L)
        set(value) {
            editor.putLong(FIRST_APP_OPEN_TIMESTAMP, value).commit()
        }

    override var lastAppOpenTimestamp: Long
        get() = sharedPref.getLong(LAST_APP_OPEN_TIMESTAMP, 0L)
        set(value) {
            editor.putLong(LAST_APP_OPEN_TIMESTAMP, value).commit()
        }

    override var lastNoonReminderIndex: Int
        get() = sharedPref.getInt(LAST_NOON_REMINDER_INDEX, -1)
        set(value) {
            editor.putInt(LAST_NOON_REMINDER_INDEX, value).commit()
        }

    override var lastEveningReminderIndex: Int
        get() = sharedPref.getInt(LAST_EVENING_REMINDER_INDEX, -1)
        set(value) {
            editor.putInt(LAST_EVENING_REMINDER_INDEX, value).commit()
        }




    override var lastNotificationPermissionDialogShownAt: Long
        get() = sharedPref.getLong(LAST_NOTIFICATION_PERMISSION_DIALOG_SHOWN_AT, 0L)
        set(value) {
            editor.putLong(LAST_NOTIFICATION_PERMISSION_DIALOG_SHOWN_AT, value).commit()
        }

    override var notificationGateDate: String
        get() = sharedPref.getString(NOTIFICATION_GATE_DATE, "") ?: ""
        set(value) {
            editor.putString(NOTIFICATION_GATE_DATE, value).commit()
        }

    override var notificationGateCount: Int
        get() = sharedPref.getInt(NOTIFICATION_GATE_COUNT, 0)
        set(value) {
            editor.putInt(NOTIFICATION_GATE_COUNT, value).commit()
        }
}