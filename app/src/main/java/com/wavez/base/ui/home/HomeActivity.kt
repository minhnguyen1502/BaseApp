package com.wavez.base.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.wavez.admob.AdsRepository
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.base.R
import com.wavez.base.ads.Ads
import com.wavez.base.ads.AppRemoteConfig
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityHomeBinding
import com.wavez.base.evenbus.UpdateVip
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.wavez.base.firebase.AppEvent
import com.wavez.base.firebase.FirebaseControlLogEvent
import com.wavez.base.ui.home.adapter.MainAdapter
import com.wavez.base.ui.home.dialog.NotiPerDialogVer2
import com.wavez.base.ui.home.notification.HomeNotificationPermissionController
import com.wavez.base.ui.premium.PremiumActivity
import com.wavez.base.ui.premium.dialogs.PurchaseSuccessDialog
import com.wavez.base.ui.setting.dialogs.RateDialog
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    override fun bindingView(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    private val notificationPermissionController by lazy {
        HomeNotificationPermissionController(sharedPref, permissionManager)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override val registerEventBus: Boolean
        get() = true

    override val adsIdNative: String
        get() = Ads.getNativeHome()

    override val nativeAdPositionKey: String
        get() = AppRemoteConfig.NATIVE_HOME_POSITION_CONFIG_KEY

    override val nativeAdView: NativeAdView
        get() {
            binding.nativeCollapse.shouldShowAd = sharedPref.nativeHomeOnOff
            return binding.nativeCollapse
        }

    override val adsIdBanner: String
        get() = Ads.getBannerHome()

    override val shouldShowBanner: Boolean
        get() = sharedPref.bannerHomeOnOff

    private var isFromSetting: Boolean = false

    companion object {
        const val PAGE_HOME = 0
        const val PAGE_SETTING = 1
        const val IS_FROM_SETTING = "is_from_setting"
        const val SHOW_PURCHASE_SUCCESS = "show_purchase_success"
        fun newIntent(
            context: Context, isFromSetting: Boolean = false, showPurchaseSuccess: Boolean = false
        ) = Intent(context, HomeActivity::class.java).apply {
            putExtra(IS_FROM_SETTING, isFromSetting)
            putExtra(SHOW_PURCHASE_SUCCESS, showPurchaseSuccess)
        }
    }

    override fun onStart() {
        if (sharedPref.nativeHomeOnOff) {
            binding.nativeCollapse.reset()
        }
        super.onStart()
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        loadAds()
        super.initConfig(savedInstanceState)

        isFromSetting = intent.getBooleanExtra(IS_FROM_SETTING, false)

        if (intent.getBooleanExtra(SHOW_PURCHASE_SUCCESS, false)) {
            PurchaseSuccessDialog.newInstance().show(
                supportFragmentManager, PurchaseSuccessDialog::class.java.simpleName
            )
        }

        if (!sharedPref.hasUserRated && sharedPref.countRate == 2 && !isFromSetting) {
            RateDialog.newInstance().show(supportFragmentManager, RateDialog::class.java.simpleName)
        }

        binding.toolbar.btnBack.gone()
        binding.toolbar.btnSave.gone()
        binding.toolbar.btnTutorial.gone()
        if (!sharedPref.isVip) binding.toolbar.btnPre.visible()

        binding.viewPager.adapter = MainAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 1
        selectPage(PAGE_HOME)
    }

    private fun loadAds() {
        if (sharedPref.adResume) {
            openAdManager.loadAd()
        } else {
            interstitialManager.loadAd()
        }
    }

    override fun initListener() {
        super.initListener()
        binding.nativeCollapse.setOnCollapseListener {
            if (adClickPercentTageManager.shouldShowAd(nativeAdPositionKey)) {
                binding.nativeCollapse.performClickAd()
                AdsRepository.onShowCollapse()
                AdsRepository.onClickCollapseNative()
            }
            binding.nativeCollapse.gone()
        }

        binding.btnHome.setOnClickListener { selectPage(PAGE_HOME) }
        binding.btnSettingNav.setOnClickListener { selectPage(PAGE_SETTING) }

        binding.toolbar.btnPre.setOnClickListener {
            startActivity(PremiumActivity.newIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedPref.firstOpenApp) {
            FirebaseControlLogEvent.logEvent(AppEvent.HOME_VIEW)
            sharedPref.firstOpenApp = false
            notificationPermissionController.requestFirstLaunchPermissionIfNeeded(
                notificationPermissionLauncher
            )
        } else {
            FirebaseControlLogEvent.logEvent(AppEvent.HOME_VIEW_RE)
            if (notificationPermissionController.shouldShowAppDialogForPendingTrigger()) {
                NotiPerDialogVer2.newInstance().show(supportFragmentManager, NotiPerDialogVer2.TAG)
            }
        }
    }

    private fun selectPage(page: Int) {
        binding.viewPager.setCurrentItem(page, false)
        binding.ivHome.isSelected = page == PAGE_HOME
        binding.tvHome.isSelected = page == PAGE_HOME
        binding.ivSetting.isSelected = page == PAGE_SETTING
        binding.tvSetting.isSelected = page == PAGE_SETTING
        binding.toolbar.tvTitle.text = getText(
            if (page == PAGE_HOME) R.string.home else R.string.setting
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateVip(event: UpdateVip) {
        if (event.isVip) {
            binding.toolbar.btnPre.gone()
        }
    }
}
