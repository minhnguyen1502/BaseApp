package com.wavez.base

//import com.google.android.libraries.places.api.Places
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.DeadSystemException
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.wavez.admob.AdsRepository
import com.wavez.admob.interstitial_ads.AdInterstitialManager
import com.wavez.admob.open_ads.OpenAdManager
import com.wavez.base.notification.reengagement.ReEngagementScheduler
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.onboard_language.OnBoardingLanguageActivity
import com.wavez.base.ui.onboarding.OnBoardingActivity
import com.wavez.base.ui.premium.repository.PremiumRepository
import com.wavez.base.ui.splash.SplashActivity
import com.wavez.base.utils.AppState
import com.wavez.base.utils.Constant
import com.wavez.base.utils.network.NetworkHelper
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var app: App

        fun instance(): App = app

        fun appContext(): Context = instance().applicationContext
    }

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    lateinit var openManager: OpenAdManager

    @Inject
    lateinit var interstitialManager: AdInterstitialManager

    @Inject
    lateinit var premiumRepository: PremiumRepository

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var reEngagementScheduler: ReEngagementScheduler

    private var currentActivity: Activity? = null
    private var startedActivityCount = 0

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val excludedActivities = listOf(
        SplashActivity::class.java,
        OnBoardingActivity::class.java,
        OnBoardingLanguageActivity::class.java,
        AdActivity::class.java,
    )

    override fun onCreate() {
        super<Application>.onCreate()
        app = this
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d("GoogleMobileAds", "initializeMobileAdsSdk: " + packageName)
        val pangleConfig = PAGConfig.Builder().appId(Constant.PANGLE_APP_ID).appIcon(R.mipmap.ic_launcher)
            .supportMultiProcess(false).setPackageName(packageName).debugLog(BuildConfig.DEBUG)
            .build()

        PAGSdk.init(applicationContext, pangleConfig, object : PAGSdk.PAGInitCallback {
            override fun success() {
                Log.i("GoogleMobileAds", "Pangle init success")
            }

            override fun fail(code: Int, msg: String?) {
                Log.e("GoogleMobileAds", "Pangle init fail: code=$code msg=$msg")
            }
        })
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
        }
        initLanguage()
        handlerDeadSystem()
        premiumRepository.startConnection()
        networkHelper.startNetworkCallback()
        initAdjustSDK()
        Log.d("ReEngagementReminder", "[App] onCreate -> ensureScheduled()")
        reEngagementScheduler.ensureScheduled()
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }

    private fun initAdjustSDK() {
        val appToken = Constant.TOKEN_ADJUST
        val environment =
            if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this, appToken, environment).apply {
            setLogLevel(LogLevel.WARN)
        }
        Adjust.initSdk(config)
    }

    private fun initLanguage() {
        CoroutineScope(Dispatchers.IO).launch {
            if (sharedPref.configLanguage.isEmpty()) {
                try {
                    val currentLocale = resources.configuration.locales[0]
                    sharedPref.configLanguage = currentLocale.language
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun handlerDeadSystem() {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                if (throwable is DeadSystemException || throwable.javaClass.name == "android.os.DeadSystemRuntimeException") {
                    return@setDefaultUncaughtExceptionHandler
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (_: Exception) {
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        AppState.isAppInForeground = true
        sharedPref.lastAppOpenTimestamp = System.currentTimeMillis()
        Log.d("ReEngagementReminder", "[App] lastAppOpenTimestamp updated -> ${sharedPref.lastAppOpenTimestamp}")
        currentActivity?.let { activity ->
            Log.d("OpenAdDebug", "===> onResume check start: ${activity::class.java.simpleName}")

            if (sharedPref.isVip) {
                Log.d("OpenAdDebug", "Blocked: User is VIP")
                return
            }

            if (AdsRepository.isAdShowFullScreen) {
                Log.d("OpenAdDebug", "Blocked: FullScreen Ad is showing")
                return
            }

            if (excludedActivities.any { it.isInstance(activity) }) {
                Log.d(
                    "OpenAdDebug",
                    "Blocked: Activity is in excluded list -> ${activity::class.java.simpleName}"
                )
                return
            }

            if (AdsRepository.isClickCollapseNativeWhenResume) {
                Log.d("OpenAdDebug", "Blocked: Click collapse native when resume")
                return
            }

            Log.d("OpenAdDebug", "PASS ALL -> showOpenAd()")
            if (sharedPref.adResume) {
                showOpenAd()
            } else {
                showInterAd()
            }
        } ?: run {
            Log.d("OpenAdDebug", "Blocked: currentActivity is NULL")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        AppState.isAppInForeground = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        coroutineScope.cancel()
    }

    private fun showOpenAd() {
        if (!sharedPref.appOpenResumeOnOff) {
            Log.d("OpenAdDebug", "Blocked: openResumeOnOff = false")
            return
        }
        val activity = currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) {
            Log.d("OpenAdDebug", "Blocked: activity is finishing/destroyed")
            return
        }
        openManager.show(activity)
        Log.d("OpenAdDebug", "showOpenAd")
    }

    private fun showInterAd() {
        val activity = currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) {
            Log.d("OpenAdDebug", "Blocked: activity is finishing/destroyed")
            return
        }

        val isSkipAd = sharedPref.isVip || !interstitialManager.isAdEligibleToShow()
        if (!sharedPref.interClickOnOff) {
            Log.d("OpenAdDebug", "Blocked: interClickOnOff = false")
            return
        }
        if (isSkipAd) {
            Log.d("OpenAdDebug", "Blocked: isSkipAd = true")
            return
        }
        interstitialManager.showAdFullScreen(activity) {}
        Log.d("OpenAdDebug", "showInterAd")
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        this.currentActivity = p0
        startedActivityCount++
    }

    override fun onActivityResumed(p0: Activity) {
        Adjust.onResume()
    }

    override fun onActivityPaused(p0: Activity) {
        Adjust.onPause()
    }

    override fun onActivityStopped(p0: Activity) {
        startedActivityCount--
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}
