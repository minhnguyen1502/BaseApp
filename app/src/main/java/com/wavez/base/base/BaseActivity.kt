package com.wavez.base.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.wavez.admob.AdsRepository
import com.wavez.admob.LoadCondition
import com.wavez.admob.ShowCondition
import com.wavez.admob.banner_ads.BannerAdManager
import com.wavez.admob.interstitial_ads.AdInterstitialManager
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.admob.native_ads.NativeHelper
import com.wavez.admob.open_ads.OpenAdManager
import com.wavez.base.R
import com.wavez.base.ads.AdClickPercentTageManager
import com.wavez.base.ads.Ads
import com.wavez.base.ads.AppRemoteConfig
import com.wavez.base.di.AppModuleEntry
import com.wavez.base.extension.goToSettingNetwork
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.home.dialog.DialogLoading
import com.wavez.base.ui.home.dialog.ReconnectNetworkDialog
import com.wavez.base.ui.onboard_language.repository.LanguageRepository
import com.wavez.base.ui.premium.PremiumViewModel
import com.wavez.base.utils.CustomToast
import com.wavez.base.utils.NetworkTracker
import com.wavez.base.utils.permission.PermissionManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(),
    ReconnectNetworkDialog.ReconnectNetworkListener {
    companion object;

    open val binding by lazy { bindingView() }

    open val registerEventBus = false

    val mainScope = CoroutineScope(Dispatchers.Main)

    val ioScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    open lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var interstitialManager: AdInterstitialManager

    @Inject
    lateinit var bannerAdManager: BannerAdManager

    @Inject
    lateinit var adClickPercentTageManager: AdClickPercentTageManager

    @Inject
    lateinit var openAdManager: OpenAdManager

    @Inject
    lateinit var showCondition: ShowCondition

    @Inject
    lateinit var loadCondition: LoadCondition

    private lateinit var networkTracker: NetworkTracker

    protected val premiumViewModel by viewModels<PremiumViewModel>()

    open val ignoreNetworkState = false

    open val triggerHasConnection = true

    open val triggerLostConnection = true

    open val adsIdBanner = Ads.getBannerHome()
    open val adsIdBannerHigh = Ads.getBannerHome()

    open val adsIdNative: String = Ads.getNativeHome()
    open val adsIdNativeHigh: String? = null

    /** Remote-config key identifying this screen's native-collapse placement (see [AppRemoteConfig]). */
    open val nativeAdPositionKey: String = AppRemoteConfig.NATIVE_INAPP_POSITION_CONFIG_KEY

    open val nativeAdView: NativeAdView? = null

    private var isLoadingNative = false
    private var nativeReloadJob: Job? = null

    @Inject
    lateinit var nativeHelper: NativeHelper

    open val shouldShowBanner = true

    var alreadyLoadedBanner = false
    private val handler2 = Handler(Looper.getMainLooper())
    private val handler = Handler(Looper.getMainLooper())
    private var loadingDialog: DialogLoading? = null
    private val loadingTag = DialogLoading::class.java.simpleName
    abstract fun bindingView(): T
    override fun onResume() {
        bannerAdManager.resumeBannerAd()
        hideNavigationButton(window)
        super.onResume()
    }

    override fun onPause() {
        bannerAdManager.pauseBannerAd()
        super.onPause()
    }

    override fun attachBaseContext(newBase: Context) {
        val languageProvider: LanguageRepository by lazy {
            EntryPointAccessors.fromApplication(
                newBase.applicationContext,
                AppModuleEntry::class.java,
            ).getLanguageRepository()
        }
        updateLocaleFormat(newBase, Locale(languageProvider.getConfigLanguageCode()))
        super.attachBaseContext(newBase)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "onCreate | activity=${this::class.java.simpleName}")
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onHandleBackPressed()
                }
            },
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        if (registerEventBus) EventBus.getDefault().register(this)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val barsType =
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            val bars = insets.getInsets(barsType)
            v.updatePadding(
                left = bars.left, top = bars.top, right = bars.right, bottom = 0
            )
            // Only consume system bars/cutout here; leave IME insets untouched so
            // descendant views (e.g. search-empty states) can react to keyboard visibility.
            WindowInsetsCompat.Builder(insets).setInsets(barsType, Insets.NONE).build()
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false
        hideNavigationButton(window)
        initView(savedInstanceState)
        initConfig(savedInstanceState)
        initListener()
        initObserver()
        networkTracker = NetworkTracker(this, lifecycle)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkTracker.isConnectedFlow.collect { hasConnection ->
                    if (ignoreNetworkState) return@collect
                    if (hasConnection) {
                        if (triggerHasConnection) getReconnectNetworkDialog()?.dismiss()
                        return@collect
                    }
                    if (triggerLostConnection) {
                        if (!sharedPref.isVip) {
                            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@collect
                            ReconnectNetworkDialog.newInstance()
                                .show(supportFragmentManager, ReconnectNetworkDialog.TAG)
                        }
                    }
                }
            }
        }

    }

    private fun getReconnectNetworkDialog(): ReconnectNetworkDialog? {
        val fragment = supportFragmentManager.findFragmentByTag(ReconnectNetworkDialog.TAG)
        if (fragment is ReconnectNetworkDialog) {
            return fragment
        }
        return null
    }

    private val TAG = "minh_banner"
    private fun loadBanner() {
        if (shouldShowBanner) {
            if (alreadyLoadedBanner) {
                findViewById<ViewGroup?>(R.id.bannerAds)?.visibility = View.VISIBLE
                return
            }
            val bannerContainer = findViewById<ViewGroup?>(R.id.bannerAds)
            bannerContainer?.let { container ->
                bannerAdManager.loadBannerAdHigh(
                    adIdHigh = adsIdBannerHigh,
                    adId = adsIdBanner,
                    onAdLoaded = { adView ->
                        alreadyLoadedBanner = true

                        val maxWidth = resources.displayMetrics.widthPixels
                        val layoutParams =
                            ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                        adView.layoutParams = layoutParams

                        container.visibility = View.VISIBLE
                        container.removeAllViews()
                        container.addView(adView)
                    },
                    onAdFailedToLoad = {
                        alreadyLoadedBanner = false
                        container.visibility = View.GONE
                    })
            } ?: Log.w(TAG, "loadBanner: bannerContainer is null, cannot load banner")

        } else {
            findViewById<ViewGroup?>(R.id.bannerAds)?.let { bannerContainer ->
                bannerContainer.visibility = View.GONE
            } ?: Log.w(TAG, "loadBanner: bannerContainer is null")
        }
    }

    private fun hideBannerAdsIfNeed(
        isPremium: Boolean
    ) {
        findViewById<ViewGroup?>(R.id.bannerAds)?.let { bannerContainer ->
            if (isPremium) {
                bannerContainer.visibility = View.GONE
            }
        }
    }

    fun loadNativeAds() {
        nativeAdView?.let { viewAd ->
            if (viewAd.isBound) {
                Log.d(
                    "NativeAds",
                    "loadNativeAds: already bound, skip reload | activity=${this::class.java.simpleName}"
                )
                return
            }

            if (!viewAd.shouldShowAd) {
                Log.d(
                    "NativeAds",
                    "loadNativeAds: shouldShowAd ${viewAd.shouldShowAd}, skip reload | activity=${this::class.java.simpleName}"
                )
                return
            }
            if (isLoadingNative) {
                Log.d(
                    "NativeAds",
                    "loadNativeAds: already loading, skip | activity=${this::class.java.simpleName}"
                )
                return
            }
            isLoadingNative = true
            val screenName = this::class.java.simpleName
            // High floor first, fall back to the regular unit on failure/timeout — same
            // ad-unit-id waterfall the onboarding native preload uses.
            val adUnitIds = listOfNotNull(adsIdNativeHigh.takeIf { !it.isNullOrEmpty() }, adsIdNative).distinct()
            Log.d(
                "NativeAds",
                "loadNativeAds: start loading, adUnitIds = $adUnitIds | activity = $screenName"
            )
            nativeHelper.load(nativeAdsIds = adUnitIds) { nativeAd ->
                Log.e(
                    "asdasd", "loadNativeAds: $adUnitIds | activity = $screenName"
                )
                if (nativeAd == null) {
                    isLoadingNative = false
                    Log.w(
                        "NativeAds",
                        "loadNativeAds: nativeAd is NULL → hidePlaceholder | activity = $screenName"
                    )
                    // Không hẹn giờ reload riêng ở đây nữa — vòng lặp liên tục trong
                    // startNativeReloadLoop() sẽ tự thử lại ở lần đếm kế tiếp.
                    viewAd.hidePlaceholder()
                } else {
                    Log.d(
                        "NativeAds",
                        "loadNativeAds: nativeAd loaded successfully → bind | activity = $screenName"
                    )
                    lifecycleScope.launch {
                        delay(NativeAdView.RELOAD_SHIMMER_MS)
                        viewAd.bind(nativeAd)
                        isLoadingNative = false
                    }
                }
            }
        } ?: Log.w(
            "NativeAds",
            "loadNativeAds: nativeAdView is NULL, skip | activity = ${this::class.java.simpleName}"
        )
    }

    // Đếm liên tục kể từ lúc vào màn (onStart): hết N giây (theo config vị trí) là reload —
    // bỏ ad đang có, load ad mới — xong lại đếm tiếp N giây kế tiếp, lặp vô hạn trong lúc màn
    // đang STARTED. onStop() cancel job này nên mỗi lần quay lại màn, đếm luôn bắt đầu lại từ 0.
    // reloadSeconds <= 0 (mặc định) nghĩa là không tự reload.
    private fun startNativeReloadLoop() {
        if (sharedPref.isVip || isFinishing || isDestroyed) return
        val reloadSeconds = sharedPref.getNativeAdPositionConfig(nativeAdPositionKey).reloadSeconds
        if (reloadSeconds <= 0) return
        nativeReloadJob?.cancel()
        nativeReloadJob = lifecycleScope.launch {
            while (isActive) {
                delay(reloadSeconds * 1000L)
                Log.d(
                    "NativeAds",
                    "reload after ${reloadSeconds}s | activity=${this@BaseActivity::class.java.simpleName}"
                )
                if (AdsRepository.isShowCollapse) {
                    AdsRepository.onDismissCollapse()
                }
                // Reset isBound trước khi gọi lại — loadNativeAds() return sớm nếu isBound
                // vẫn true (ad cũ chưa được "đóng"). showPlaceholder() (thay vì hidePlaceholder())
                // để hiện shimmer ngay khi bắt đầu reload, giữ nguyên tới lúc bind() ad mới —
                // nếu dùng hidePlaceholder() thì cả view bị ẩn trắng suốt lúc load + 300ms delay,
                // shimmer không bao giờ thấy được.
                nativeAdView?.let {
                    it.isBound = false
                    it.showPlaceholder()
                }
                loadNativeAds()
            }
        }
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initConfig(savedInstanceState: Bundle?) {}

    open fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                premiumViewModel.isPremium.collect {
                    onVipChangedState(it)
                }
            }
        }
    }

    fun showLoading(onCancelled: (() -> Unit)? = null) {
        if (isFinishing || isDestroyed) return
        val fm = supportFragmentManager
        if (fm.findFragmentByTag(loadingTag) != null) return
        val dialog = DialogLoading.newIntent()
        dialog.onDismissed = onCancelled
        fm.beginTransaction().add(dialog, loadingTag).commitNowAllowingStateLoss()
        loadingDialog = dialog
    }

    fun dismissLoading() {
        if (isFinishing || isDestroyed) return
        val fm = supportFragmentManager
        val dialog = fm.findFragmentByTag(loadingTag) as? DialogLoading ?: loadingDialog
        dialog?.onDismissed = null  // prevent cancelImport() on programmatic dismiss
        dialog?.dismissAllowingStateLoss()
        loadingDialog = null
    }

    fun showToast(contentId: Int, gravity: Int) {
        val toast = CustomToast.makeText(
            context = this,
            contentId = contentId,
            bgDrawable = R.drawable.bg_toast,
            gravity = gravity,
        )
        toast.show()
    }

    protected open fun onVipChangedState(isPremium: Boolean) {
        hideBannerAdsIfNeed(isPremium = isPremium)
    }

    open fun release() {}

    open fun onHandleBackPressed() {
        AdsRepository.onBackScreen()
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
            return
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onStart() {
        Log.d("Lifecycle", "onStart | activity=${this::class.java.simpleName}")
        super.onStart()
        loadBanner()
        Log.d("NativeAds", "onStart: isShowCollapse = ${AdsRepository.isShowCollapse}")
        if (AdsRepository.isShowCollapse) {
            AdsRepository.onDismissCollapse()
        }
        loadNativeAds()
        // Bắt đầu đếm lại từ 0 mỗi lần vào/quay lại màn — xem startNativeReloadLoop().
        startNativeReloadLoop()
    }

    private fun updateLocaleFormat(c: Context, localeToSwitchTo: Locale): ContextWrapper {
        var context = c
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        val localeList = LocaleList(localeToSwitchTo)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context = context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION") resources.updateConfiguration(
                configuration,
                resources.displayMetrics,
            )
        }
        return ContextWrapper(context)
    }

    fun navigateTo(
        intent: Intent,
        forceShowAd: Boolean = false,
        shouldFinishCurrentActivity: Boolean = false,
    ) {
        val isSkipAd =
            !showCondition.shouldShow() || !interstitialManager.isAdEligibleToShow()
        Log.d(
            "InterstitialAd", """
    shouldShow=${showCondition.shouldShow()}
    isEligible=${interstitialManager.isAdEligibleToShow()}
    """.trimIndent()
        )
        if (isSkipAd) {
            startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
            return
        }

        showLoadingAds()
        handler.postDelayed({
            val showAdAction: (() -> Unit) -> Unit = if (forceShowAd) {
                { onContinue ->
                    // not gap
                    interstitialManager.forceShowAdFullScreen(this, onContinue)
                }
            } else {
                { onContinue ->
                    interstitialManager.showAdFullScreen(this, onContinue)
                }
            }
            dismissLoadingAds()
            showAdAction {
                startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
            }
        }, 1000)

    }

    private fun startAndFinishCurrentIfNeed(intent: Intent, shouldFinishCurrentActivity: Boolean) {
        startActivity(intent)
        if (shouldFinishCurrentActivity) {
            finish()
        }
    }

    private var loadingView: View? = null

    fun showLoadingAds() {
        Log.e("adasdas", "showLoading: ")
        if (loadingView != null) return
        val root = window.decorView as ViewGroup
        loadingView = View.inflate(this, R.layout.layout_loading_fullscreen, null)

        root.addView(
            loadingView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun dismissLoadingAds() {
        Log.e("adasdas", "hideLoading: ")
        loadingView?.let {
            val root = window.decorView as ViewGroup
            root.removeView(it)
            loadingView = null
        }
    }

    fun onBackWithAds(onBack: () -> Unit) {
        val isSkipAd = sharedPref.isVip || !interstitialManager.isAdEligibleToShow()
        if (isSkipAd) {
            onBack()
            return
        }
        showLoadingAds()
        handler2.postDelayed({
            dismissLoadingAds()
            interstitialManager.showAdFullScreen(this, onAdComplete = {
                onBack()
            })
        }, 1000)
    }

    override fun onStop() {
        nativeReloadJob?.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        handler2.removeCallbacksAndMessages(null)
        handler.removeCallbacksAndMessages(null)
        if (registerEventBus) EventBus.getDefault().unregister(this)
        release()
        super.onDestroy()
    }

//    private fun releaseNativeAds() {
//        nativeDisplayControllers2.forEach {
//            it.cleanupAdDisplay()
//        }
//        nativeDisplayControllers2.clear()
//        nativeAdViewModel.releaseAds(NativeAdPosition.NATIVE_COLLAPSE)
//    }

    override fun onReconnectNetwork() {
        goToSettingNetwork()
    }

    fun hideNavigationButton(window: Window) {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}