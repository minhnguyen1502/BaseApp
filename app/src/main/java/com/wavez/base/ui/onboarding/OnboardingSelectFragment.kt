package com.wavez.base.ui.onboarding

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.wavez.admob.NativeAdViewModel
import com.wavez.admob.native_ads.NativeDisplayController2
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.R
import com.wavez.base.base.BaseFragment
import com.wavez.base.databinding.FragmentChooseFeatureBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.wavez.base.firebase.AppEvent
import com.wavez.base.firebase.FirebaseControlLogEvent
import com.wavez.base.storage.onboarding.OnboardingProvider
import com.wavez.base.ui.onboarding.adapter.HabitsAdapter
import com.wavez.base.utils.CustomToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.text.set

@AndroidEntryPoint
class OnboardingSelectFragment : BaseFragment<FragmentChooseFeatureBinding>() {

    override fun bindingView() = FragmentChooseFeatureBinding.inflate(layoutInflater)

    companion object {

        fun newInstance() =
            OnboardingSelectFragment()
    }

    private val habitsAdapter by lazy {
        HabitsAdapter()
    }

    private var isClickFirstOnboarding1: Boolean = false

    protected val nativeAdViewModel by viewModels<NativeAdViewModel>()

    private var nativeDisplayController2 = mutableListOf<NativeDisplayController2>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        Glide.with(requireContext()).load(R.drawable.bg_app).centerCrop().into(binding.bgApp)
        // Load & show: chỉ request native khi màn hình thật sự hiển thị (tăng show rate)
        if (sharedPref.nativeOnboarding1OnOff) {
            nativeAdViewModel.loadAd(NativeAdPosition.ONBOARDING_1)
        }
        initViewOnboarding1()
    }

    override fun initObserver() {
        super.initObserver()
        nativeAdViewModel.getNativeAdLiveData(NativeAdPosition.ONBOARDING_1)
            .observe(this) { nativeAd ->
                initNativeAds1()
            }

        nativeAdViewModel.getNativeAdLiveData(NativeAdPosition.ONBOARDING_1_CLICK)
            .observe(this) { nativeAd ->
                if (isClickFirstOnboarding1) {
                    initNativeAds2()
                }
            }

    }

    override fun onResume() {
        FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_VIEW)
        super.onResume()
    }


    private fun initViewOnboarding1() {
        binding.rcvHabits.adapter = habitsAdapter
        habitsAdapter.set(OnboardingProvider.getHabits())
        habitsAdapter.onItemSelected = {
            val conditionAccessHabit = habitsAdapter.getSelectedItems().isNotEmpty()
            if (!isClickFirstOnboarding1) {
                if (sharedPref.nativeOnb1ClickOnOff) {
                    nativeAdViewModel.loadAd(NativeAdPosition.ONBOARDING_1_CLICK)
                }
                initNativeAds2()
                isClickFirstOnboarding1 = true
            }
            updateState(conditionAccessHabit)
        }
    }

    private fun updateState(conditionAccessHabit: Boolean) {
        binding.btnNext.isSelected = conditionAccessHabit
    }

    override fun initListener() {
        super.initListener()

        binding.btnNext.setOnClickListener {
            if (habitsAdapter.getSelectedItems().isEmpty()) {
                showToastFirstChoose()
            } else {
                OnboardingProvider.pushEvent(habitsAdapter.getSelectedItems())
                nextScreen()
            }
        }
    }

    private fun nextScreen() {
        (requireActivity() as OnBoardingActivity).nextScreen()
    }

    private fun showToastFirstChoose() {
        val toast = CustomToast.makeText(
            context = requireContext(),
            contentId = R.string.please_choose_at_least_one_option,
            bgDrawable = R.drawable.bg_toast,
            gravity = Gravity.TOP,
        )
        toast.show()
    }

    private fun initNativeAds1() {
        val adView = binding.bigNativeAdView
        adView.shouldShowAd = sharedPref.nativeOnboarding1OnOff
        val nativeDisplayController = NativeDisplayController2(
            nativeAdViewModel.getNativeAdPlaceholder2(NativeAdPosition.ONBOARDING_1), adView
        )
        nativeDisplayController.setupAdDisplay()
        nativeDisplayController2.add(nativeDisplayController)
    }

    private fun initNativeAds2() {
        val nativeAd = nativeAdViewModel.getNativeAdPlaceholder2(NativeAdPosition.ONBOARDING_1_CLICK)
        val adView = binding.bigNativeClick

        val shouldShowAd = sharedPref.nativeOnb1ClickOnOff
        adView.shouldShowAd = shouldShowAd

        if (nativeAd.nativeAd == null) {
            adView.gone()
            return
        }
        binding.bigNativeAdView.gone()
        adView.visible()

        val nativeDisplayController = NativeDisplayController2(
            nativeAd,
            adView
        )
        nativeDisplayController.setupAdDisplay()
        nativeDisplayController2.add(nativeDisplayController)
    }
    override fun onDestroy() {
        super.onDestroy()
        nativeDisplayController2.forEach {
            it.cleanupAdDisplay()
        }
    }
}