package com.wavez.base.ui.onboard_language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wavez.admob.NativeAdViewModel
import com.wavez.admob.native_ads.NativeDisplayController2
import com.wavez.admob.native_ads.event.NativeAdPosition
import com.wavez.base.R
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityLanguageBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.invisible
import com.wavez.base.extension.visible
import com.wavez.base.firebase.AppEvent
import com.wavez.base.firebase.FirebaseControlLogEvent
import com.wavez.base.ui.onboard_language.adapter.LanguageAdapter
import com.wavez.base.ui.onboard_language.model.DataResult
import com.wavez.base.ui.onboard_language.model.Language
import com.wavez.base.ui.onboard_language.viewmodel.LanguageViewModel
import com.wavez.base.ui.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.code
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class OnBoardingLanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    override fun bindingView(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<LanguageViewModel>()

    lateinit var adapter: LanguageAdapter
    private val nativeAdViewModel by viewModels<NativeAdViewModel>()
    private var isSelectedLanguage = false
    private var nativeDisplayController2 = mutableListOf<NativeDisplayController2>()

    companion object {
        private const val SET_LANGUAGE_LOADING_DELAY = 1000L
        private const val APPLY_LANGUAGE_DELAY = 3000L

        fun newIntent(context: Context) = Intent(context, OnBoardingLanguageActivity::class.java)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        if (sharedPref.nativeLanguage1OnOff) {
            nativeAdViewModel.loadAd(NativeAdPosition.LANGUAGE)
        }
        initAdapter()
        binding.toolbar.btnBack.gone()
        binding.toolbar.btnSave.gone()
        viewModel.getUiLanguages()
    }

    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSave.setOnClickListener {
            if (!isSelectedLanguage) {
                showToast(R.string.please_select_your_language, Gravity.TOP)
                return@setOnClickListener
            }
            FirebaseControlLogEvent.logEvent(AppEvent.LFO_CLICK_NEXT)
            viewModel.saveConfig()
            applySelectedLanguage()
        }
    }

    override fun initObserver() {
        super.initObserver()

        nativeAdViewModel.getNativeAdLiveData(NativeAdPosition.LANGUAGE).observe(this) { nativeAd ->
            initNativeAds1()

        }

        nativeAdViewModel.getNativeAdLiveData(NativeAdPosition.LANGUAGE_2).observe(this) { _ ->
            if (isSelectedLanguage) {
                initNativeAds2()
            }
        }

        viewModel.selectedUiLanguage.observe(this) {
            adapter.onLanguageSelected(it.code)
        }

        viewModel.uiLanguageState.observe(this) {
            binding.progressBar.gone()
            when (it) {
                is DataResult.Loading -> {
                    binding.progressBar.visible()
                }

                is DataResult.Success -> {
                    binding.progressBar.gone()
                    adapter.setData(it.data)
                }

                is DataResult.Error -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseControlLogEvent.logEvent(AppEvent.LFO_VIEW)
    }

    private fun initNativeAds1() {
        val nativeAd = nativeAdViewModel.getNativeAdPlaceholder2(NativeAdPosition.LANGUAGE)
        val adView = binding.bigNativeAdView
        val nativeDisplayController = NativeDisplayController2(
            nativeAd, adView
        ) { sharedPref.nativeLanguage1OnOff }
        nativeDisplayController.setupAdDisplay()
        nativeDisplayController2.add(nativeDisplayController)
    }

    private fun initNativeAds2() {
        val nativeAd = nativeAdViewModel.getNativeAdPlaceholder2(NativeAdPosition.LANGUAGE_2)
        val adView = binding.bigNativeAdView2
        if (nativeAd.nativeAd == null) {
            adView.gone()
            return
        }
        adView.visible()
        val nativeDisplayController = NativeDisplayController2(
            nativeAd, adView
        )
        nativeDisplayController.setupAdDisplay()
        nativeDisplayController2.add(nativeDisplayController)
        if (nativeAd.nativeAd != null) {
            binding.bigNativeAdView.gone()
        }
    }

    private fun initAdapter() {
        binding.toolbar.btnSave.isSelected = false
        adapter = LanguageAdapter().apply {
            onSelected = {
                if (!isSelectedLanguage) {
                    FirebaseControlLogEvent.logEvent(AppEvent.LFO_CLICK_LANGUAGE)
                    if (sharedPref.nativeLanguage2OnOff) {
                        nativeAdViewModel.loadAd(NativeAdPosition.LANGUAGE_2)
                    }
                    initNativeAds2()
                    isSelectedLanguage = true
                    showSetLanguageLoading()
                }
                viewModel.onLanguageChanged(it)
                binding.toolbar.btnSave.isSelected = true
            }
        }
        binding.rvLanguage.adapter = adapter
    }

    private fun showSetLanguageLoading() {
        binding.toolbar.btnSave.gone()
        binding.toolbar.loadingSetLanguage.visible()
        lifecycleScope.launch {
            delay(SET_LANGUAGE_LOADING_DELAY.milliseconds)
            binding.toolbar.loadingSetLanguage.gone()
            binding.toolbar.btnSave.visible()
        }
    }

    private fun applySelectedLanguage() {
        viewModel.selectedUiLanguage.value?.let { bindSelectedLanguageInfo(it) }
        binding.layoutSelectLanguage.gone()
        binding.toolbar.btnSave.gone()
        binding.layoutApplyLanguage.visible()
        lifecycleScope.launch {
            delay(APPLY_LANGUAGE_DELAY.milliseconds)
            startActivity(OnBoardingActivity.newIntent(this@OnBoardingLanguageActivity))
            finish()
        }
    }

    private fun bindSelectedLanguageInfo(language: Language) {
        binding.tvLanguageName.text = getString(language.englishName)
        if (language.nativeName == -1) {
            binding.tvLanguage.invisible()
        } else {
            binding.tvLanguage.visible()
            binding.tvLanguage.text = getString(language.nativeName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseNativeAds2()
    }

    private fun releaseNativeAds2() {
        nativeDisplayController2.forEach {
            it.cleanupAdDisplay()
        }
    }
}
