package com.wavez.base.ui.onboarding


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.wavez.admob.NativeAdViewModel
import com.wavez.admob.interstitial_ads.AdInterstitialSplashManager
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.ads.FullScreenNativeAdOnboardingFragment
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityWalkThroughBinding
import com.wavez.base.storage.onboarding.OnboardingProvider
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.onboarding.adapter.OnboardingViewPagerAdapter
import com.wavez.base.ui.premium.PremiumActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityWalkThroughBinding>() {
    override fun bindingView(): ActivityWalkThroughBinding {
        return ActivityWalkThroughBinding.inflate(layoutInflater)
    }

    companion object {

        fun newIntent(
            context: Context,
        ): Intent {
            return Intent(context, OnBoardingActivity::class.java)
        }

    }

    private val fragments = ArrayList<Fragment>()

    private val nativeAdViewModel by viewModels<NativeAdViewModel>()

    @Inject
    lateinit var interSplashManager: AdInterstitialSplashManager

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        preLoadAds()
        initPage()
    }

    private fun preLoadAds() {
        if (sharedPref.interSplashOnOff) {
            interSplashManager.loadAd()
        }
    }

    override fun initListener() {
        super.initListener()

        binding.ivClose.setOnClickListener {
            val currentFragment = fragments.getOrNull(binding.viewPager.currentItem)
            (currentFragment as? FullScreenNativeAdOnboardingFragment)?.triggerCloseClickBait()
            nextScreen()
        }
    }

    fun nextScreen() {
        if (binding.viewPager.currentItem == fragments.lastIndex) {
            sharedPref.isNeedShowLanguage = false
            val intent = if (sharedPref.isVip) {
                HomeActivity.newIntent(this@OnBoardingActivity)
            } else {
                PremiumActivity.newIntent(
                    this@OnBoardingActivity,
                    true,
                )
            }
//            val intent = HomeActivity.newIntent(this@OnBoardingActivity)
            startActivity(intent)
            finish()
        } else {
            val next = binding.viewPager.currentItem + 1
            Log.d("OnBoarding", "Move to next page = $next")

            binding.viewPager.setCurrentItem(next, true)
        }
    }

    private fun initPage() {
        fragments.add(
            OnboardingSelectFragment.newInstance(),
        )
        fragments.add(OnBoardingDetailFragment.newInstance(OnboardingProvider.ONBOARD_02))
        if (sharedPref.nativeFull1OnOff) {
            fragments.add(
                FullScreenNativeAdOnboardingFragment.newInstance(
                    NativeAdPosition.ONBOARDING_NATIVE_FULL_1
                )
            )
        }

        fragments.add(OnBoardingDetailFragment.newInstance(OnboardingProvider.ONBOARD_03))
        if (sharedPref.nativeFull2OnOff) {
            fragments.add(
                FullScreenNativeAdOnboardingFragment.newInstance(
                    NativeAdPosition.ONBOARDING_NATIVE_FULL_2
                )
            )
        }
        fragments.add(OnBoardingDetailFragment.newInstance())

        binding.viewPager.adapter = OnboardingViewPagerAdapter(this, fragments)
        binding.viewPager.isUserInputEnabled = false

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentFragment = fragments.getOrNull(position)
                if (currentFragment is FullScreenNativeAdOnboardingFragment) {
                    currentFragment.runWhenViewResumed {
                        currentFragment.onFragmentVisible()
                    }
                }
                val isOnboardingDetail = currentFragment is OnBoardingDetailFragment
//                binding.viewPager.isUserInputEnabled = !isOnboardingDetail && position != 0
                binding.ivClose.isVisible = !isOnboardingDetail && position != 0
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

        })
    }
}