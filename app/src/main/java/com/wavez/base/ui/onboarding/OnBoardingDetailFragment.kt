package com.wavez.base.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseFragment
import com.wavez.base.databinding.FragmentOnBoardingDetailBinding
import com.wavez.base.extension.getParcel
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.wavez.base.firebase.AppEvent
import com.wavez.base.firebase.FirebaseControlLogEvent
import com.wavez.base.storage.onboarding.BoardingData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingDetailFragment : BaseFragment<FragmentOnBoardingDetailBinding>() {
    override fun bindingView(): FragmentOnBoardingDetailBinding {
        return FragmentOnBoardingDetailBinding.inflate(layoutInflater)
    }

    companion object {

        private const val ARG_DATA = "arg_data"

        private const val SHOULD_SHOW_NATIVE_AD = "should_show_native_ad"

        fun newInstance(boardingData: BoardingData? = null, shouldShowNativeAd: Boolean = false) =
            OnBoardingDetailFragment().apply {
                arguments = bundleOf(
                    ARG_DATA to boardingData,
                    SHOULD_SHOW_NATIVE_AD to shouldShowNativeAd,
                )
            }
    }

    private var boardingData: BoardingData? = null

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        boardingData = arguments?.getParcel(ARG_DATA)
        boardingData?.let {
            initViewOnboardingAll(it)
        } ?: run {
            binding.viewObd.gone()
            binding.viewWelcome.visible()
            Glide.with(requireContext()).load(R.drawable.img_welcome).into(binding.imgWelcome)
        }
    }

    override fun onResume() {
        super.onResume()
        logScreen()
    }

    private fun logScreen() {
        boardingData?.let {
            when (it.index) {
                2 -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_VIEW)
                else -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_VIEW)
            }
        } ?: run {
            FirebaseControlLogEvent.logEvent(AppEvent.WELCOME_VIEW)
        }

    }

    private fun initViewOnboardingAll(boardingData: BoardingData) {
        binding.viewObd.visible()
        binding.viewWelcome.gone()
        binding.btnNext.isSelected = true
        Glide.with(this).load(R.drawable.bg_onb).centerCrop().into(binding.imgBackground)
        Glide.with(this).load(boardingData.imageID).into(binding.imgOnb)
        binding.tvTitle.text = getString(boardingData.labelID)
        binding.tvDes.text = getString(boardingData.desID)

    }

    override fun initListener() {
        super.initListener()

        binding.btnNext.setOnClickListener {
            nextScreen()
        }

        binding.btnGetStarted.setOnClickListener {
            FirebaseControlLogEvent.logEvent(AppEvent.WELCOME_CLICK)
            nextScreen()
        }
    }

    private fun nextScreen() {
        when (boardingData?.index) {
            1 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK_NEXT)
            }

            2 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_CLICK_NEXT)
            }

            3 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_CLICK_NEXT)
            }
        }
        (requireActivity() as OnBoardingActivity).nextScreen()
    }
}
