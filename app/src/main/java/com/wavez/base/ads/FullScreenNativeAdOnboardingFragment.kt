package com.wavez.base.ads

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wavez.admob.NativeAdViewModel
import com.wavez.admob.native_ads.NativeAdLoadState
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.base.BaseFragment
import com.wavez.base.databinding.FragmentFullScreenNativeAdBinding
import com.wavez.base.extension.gone
import com.wavez.base.ui.onboarding.OnBoardingActivity
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class FullScreenNativeAdOnboardingFragment : BaseFragment<FragmentFullScreenNativeAdBinding>() {

    override fun bindingView(): FragmentFullScreenNativeAdBinding {
        return FragmentFullScreenNativeAdBinding.inflate(layoutInflater)
    }

    companion object {

        private const val ARG_POSITION = "native_ad_position"

        fun newInstance(position: NativeAdPosition): FullScreenNativeAdOnboardingFragment {
            return FullScreenNativeAdOnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POSITION, position.name)
                }
            }
        }
    }

    protected val nativeAdViewModel by activityViewModels<NativeAdViewModel>()

    private var hasTriggeredNextScreen = false
    private var hasRequestedAd = false
    private var nativeAdReady: NativeAd? = null

    private val adType: NativeAdPosition by lazy {
        val name = arguments?.getString(ARG_POSITION)
        runCatching {
            NativeAdPosition.valueOf(
                name ?: ""
            )
        }.getOrElse { NativeAdPosition.ONBOARDING_NATIVE_FULL_1 }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.btnClose.gone()
    }

    override fun initObserver() {
        super.initObserver()

        Log.d("Log_ad_full", "initObserver: $adType")
        nativeAdViewModel.getNativeAdLiveData(adType).observe(viewLifecycleOwner) { nativeAd ->
            nativeAdReady = nativeAd
            if (nativeAdReady != null) {
                binding.loadingLayout.gone()
                binding.fullScreenNativeAdView.bind(nativeAdReady!!)
            }
        }
    }

    fun onFragmentVisible() {
        if (hasTriggeredNextScreen || hasRequestedAd || nativeAdReady != null) return
        hasRequestedAd = true
        // Load & show: chỉ request native full khi page hiển thị, giữ loadingLayout tới khi có ad
        nativeAdViewModel.loadAd(adType)
        viewLifecycleOwner.lifecycleScope.launch {
            val state = nativeAdViewModel.getNativeAdPlaceholder2(adType).nativeAdFlow
                .first { it != NativeAdLoadState.Loading }
            val hasAd = state == NativeAdLoadState.Loaded &&
                    nativeAdViewModel.getNativeAdPlaceholder2(adType).nativeAd != null
            // isResumed: ViewPager2 chỉ RESUME page hiện tại -> tránh skip nhầm khi user đã bấm close
            if (!hasAd && !hasTriggeredNextScreen && isResumed) {
                hasTriggeredNextScreen = true
                (activity as? OnBoardingActivity)?.nextScreen()
            }
        }
    }

    fun triggerCloseClickBait() {
        if (nativeAdReady == null) return
        if (Random.nextInt(100) >= sharedPref.adShow) return
        binding.fullScreenNativeAdView.performClickAd()
    }

    fun runWhenViewResumed(action: () -> Unit) {
        if (!isAdded || view == null) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                action()
            }
        }
    }
}
