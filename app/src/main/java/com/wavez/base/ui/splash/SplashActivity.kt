package com.wavez.base.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wavez.admob.Gdpr
import com.wavez.admob.GoogleMobileAdsConsentManager
import com.wavez.admob.NativeAdViewModel
import com.wavez.admob.interstitial_ads.AdInterstitialSplashManager
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.R
import com.wavez.base.ads.Ads
import com.wavez.base.ads.AppRemoteConfig
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivitySplashBinding
import com.wavez.base.extension.gone
import com.wavez.base.firebase.AppEvent
import com.wavez.base.firebase.FirebaseControlLogEvent
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.onboard_language.OnBoardingLanguageActivity
import com.wavez.base.ui.premium.PremiumActivity
import com.wavez.base.ui.premium.repository.PremiumRepository
import com.wavez.base.utils.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun bindingView(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    companion object {
        private const val DELAY_TO_CHECK_AD = 100L

        private const val MIN_DELAY_TIME = 3000L

        private const val REMOTE_CONFIG_WAIT_TIMEOUT_MS = 8000L

        fun newIntent(context: Context) = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    @Inject
    lateinit var interSplashManager: AdInterstitialSplashManager

    @Inject
    lateinit var appRemoteConfig: AppRemoteConfig

    @Inject
    lateinit var appShared: SharedPref

    @Inject
    lateinit var premiumRepository: PremiumRepository

    private var launchStartedTime = 0L

    private val nativeAdViewModel by viewModels<NativeAdViewModel>()

    // Hoàn tất khi fetchRemoteConfig() đã ghi xong (hoặc fail) giá trị xuống SharedPref — loadAllAds()
    // đợi cờ này trước khi đọc các onOff/isTestAdsMode, tránh đọc phải giá trị mặc định/stale do race
    // giữa fetch remote config (async, qua network) và flow GDPR/purchase-check (chỉ đợi tối thiểu 3s).
    private val remoteConfigReady = CompletableDeferred<Unit>()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        sharedPref.countRate += 1
        sharedPref.isDevMode = DeviceUtils.isDevEnvironment(this)
        Glide.with(this).load(R.drawable.bg_splash).centerCrop().into(binding.ivBg)
        appRemoteConfig.fetchRemoteConfig { remoteConfigReady.complete(Unit) }
        binding.tvThisActionAds.isVisible = !sharedPref.isVip
        Glide.with(this).load(R.drawable.logo_app).placeholder(R.drawable.logo_app)
            .into(binding.ivApp)
        getInstanceGdpr()
    }

    private fun goToNextActivity() {
        if (sharedPref.isNeedShowLanguage) {
            startActivity(Intent(this, OnBoardingLanguageActivity::class.java))
        } else if (!sharedPref.isNeedShowLanguage && !sharedPref.isVip) {
            startActivity(PremiumActivity.newIntent(this, true))
        } else {
            startActivity(HomeActivity.newIntent(this))
        }
        finish()
    }

    private fun getInstanceGdpr() {
        lifecycleScope.launch {
            val start = System.currentTimeMillis()
            // Chờ xác nhận trạng thái gói thật từ Google Play (queryPurchases() bắn từ
            // WtApp.onCreate) trước khi đọc sharedPref.isVip, để không quyết định dựa trên
            // cache local cũ (vd gói vừa hết hạn/huỷ nhưng local vẫn còn ghi isVip = true).
            premiumRepository.awaitFirstPurchaseCheck(MIN_DELAY_TIME)
            Log.e("aawawawaw", "getInstanceGdpr: isVip=${sharedPref.isVip}")

            if (sharedPref.isVip) {
                binding.bannerAds.root.gone()
                val elapsed = System.currentTimeMillis() - start
                if (elapsed < MIN_DELAY_TIME) {
                    delay(MIN_DELAY_TIME - elapsed)
                }
                goToNextActivity()
                return@launch
            }

            launchStartedTime = System.currentTimeMillis()
            val googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(this@SplashActivity)
            googleMobileAdsConsentManager.gatherConsent(this@SplashActivity) { consentError ->
                if (consentError != null) {
                }
                sharedPref.canRequestAds = googleMobileAdsConsentManager.canRequestAds
                Gdpr.consent(this@SplashActivity, sharedPref.canRequestAds)
                lifecycleScope.launch {
                    withTimeoutOrNull(REMOTE_CONFIG_WAIT_TIMEOUT_MS) { remoteConfigReady.await() }
                    loadAllAds()
                    showInterstitialIfAvailable()
                }
            }
        }
    }

    private fun initNativeAd() {
        val nativeAdConfigs = mapOf(
            NativeAdPosition.LANGUAGE to Pair(
                Ads.getListAdNativeLanguageAds1(), sharedPref.nativeLanguage1OnOff
            ),
            NativeAdPosition.LANGUAGE_2 to Pair(
                Ads.getListAdNativeLanguageAds2(), sharedPref.nativeLanguage2OnOff
            ),
            NativeAdPosition.ONBOARDING_1 to Pair(
                Ads.getListAdNativeOnboarding1(), sharedPref.nativeOnboarding1OnOff
            ),
            NativeAdPosition.ONBOARDING_1_CLICK to Pair(
                Ads.getListAdNativeOnboarding1Click(), sharedPref.nativeOnb1ClickOnOff
            ),
            NativeAdPosition.ONBOARDING_NATIVE_FULL_1 to Pair(
                Ads.getListAdNativeObdFull1(), sharedPref.nativeFull1OnOff
            ),
            NativeAdPosition.ONBOARDING_NATIVE_FULL_2 to Pair(
                Ads.getListAdNativeObdFull2(), sharedPref.nativeFull2OnOff
            ),
        )
        nativeAdConfigs.forEach { (position, pair) ->
            val (adIds, shouldLoad) = pair
            if (shouldLoad) {
                nativeAdViewModel.setNativeAdIds(position, adIds)
            }
        }
    }

    private fun loadAllAds() {
        loadBannerSplash()
        initNativeAd()
        if (sharedPref.interSplashOnOff) {
            interSplashManager.loadAd()
        }
    }

    private fun showInterstitialIfAvailable() {
        lifecycleScope.launch {
            delay(MIN_DELAY_TIME)
            if (!sharedPref.interSplashOnOff) {
                goToNextActivity()
                return@launch
            }
            while (isActive) {
                val elapsed = System.currentTimeMillis() - launchStartedTime
                if (interSplashManager.isAdReady() || elapsed >= sharedPref.splashMaxWaitMs) {
                    showInterstitialAdThenNavigate()
                    break
                }
                delay(DELAY_TO_CHECK_AD)
            }
        }
    }

    private fun showInterstitialAdThenNavigate() {
        interSplashManager.showAdFullScreen(this) {
            goToNextActivity()
        }
    }

    private fun loadBannerSplash() {
        if (!sharedPref.bannerSplashOnOff) {
            binding.bannerAds.root.gone()
            return
        }
        val bannerContainer = binding.bannerAds.root
        bannerAdManager.loadBannerAdHigh(
            adIdHigh = Ads.getBannerAdsSplash(),
            adId = Ads.getBannerAdsSplash(),
            onAdLoaded = { adView ->
                val maxWidth = resources.displayMetrics.widthPixels
                val layoutParams =
                    ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                adView.layoutParams = layoutParams
                bannerContainer.visibility = View.VISIBLE
                bannerContainer.removeAllViews()
                bannerContainer.addView(adView)
            },
            onAdFailedToLoad = {
                bannerContainer.visibility = View.GONE
            },
        )
    }

    override fun onResume() {
        if (sharedPref.firstOpenApp) {
            FirebaseControlLogEvent.logEvent(AppEvent.SPLASH_VIEW)
        } else {
            FirebaseControlLogEvent.logEvent(AppEvent.SPLASH_VIEW_RE)
        }
        bannerAdManager.resumeBannerAd()
        super.onResume()
    }

    override fun onPause() {
        bannerAdManager.pauseBannerAd()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdManager.destroyBannerAd()
        super.onDestroy()
    }

}

