package com.wavez.base.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.wavez.admob.ShowCondition
import com.wavez.admob.interstitial_ads.AdInterstitialManager
import com.wavez.base.R
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.premium.PremiumViewModel
import com.wavez.base.utils.CustomToast
import com.wavez.base.utils.permission.PermissionManager
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    private var _binding: T? = null

    protected val binding get() = _binding!!
    @Inject
    open lateinit var permissionManager: PermissionManager
    open val registerEventBus = false
    protected val premiumViewModel by viewModels<PremiumViewModel>()
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var interstitialManager: AdInterstitialManager

    @Inject
    lateinit var showCondition: ShowCondition

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = bindingView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
        if (registerEventBus) EventBus.getDefault().register(this)
        initConfig(view, savedInstanceState)
        initListener()
        initObserver()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        release()
        if (registerEventBus) EventBus.getDefault().unregister(this)
        _binding = null
        super.onDestroyView()
    }

    open fun initView(view: View, savedInstanceState: Bundle?) {}

    open fun initObserver() {
        lifecycleScope.launch {
            premiumViewModel.isPremium.collect {

            }
        }
    }

    open fun initConfig(view: View, savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun release() {}

    abstract fun bindingView(): T

    private var loadingView: View? = null

    fun showLoadingAds() {
        Log.e("adasdas", "showLoading: ")
        if (loadingView != null) return
        val root = requireActivity().window.decorView as ViewGroup
        loadingView = View.inflate(requireContext(), R.layout.layout_loading_fullscreen, null)

        root.addView(
            loadingView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun dismissLoadingAds() {
        Log.e("adasdas", "hideLoading: ")
        loadingView?.let {
            val root = requireActivity().window.decorView as ViewGroup
            root.removeView(it)
            loadingView = null
        }
    }

    fun navigateTo(
        intent: Intent,
        forceShowAd: Boolean = false,
        shouldFinishCurrentActivity: Boolean = false,
    ) {
        val isSkipAd =
            !showCondition.shouldShow() || !interstitialManager.isAdEligibleToShow()
        if (isSkipAd) {
            startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
            return
        }

        showLoadingAds()
        handler.postDelayed({
            val showAdAction: (() -> Unit) -> Unit = if (forceShowAd) {
                { onContinue ->
                    interstitialManager.forceShowAdFullScreen(requireActivity(), onContinue)
                }
            } else {
                { onContinue ->
                    interstitialManager.showAdFullScreen(requireActivity(), onContinue)
                }
            }
            dismissLoadingAds()
            showAdAction {
                if (isAdded && !isDetached) {
                    startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
                }
            }
        }, 1000)

    }

    private fun startAndFinishCurrentIfNeed(intent: Intent, shouldFinishCurrentActivity: Boolean) {
        startActivity(intent)
        if (shouldFinishCurrentActivity) {
            requireActivity().finish()
        }
    }

    fun showInterstitialAd(onContinue: () -> Unit) {
        val isSkipAd =
            !showCondition.shouldShow() || !interstitialManager.isAdEligibleToShow()
        if (isSkipAd) {
            onContinue()
            return
        }

        showLoadingAds()
        handler.postDelayed({
            dismissLoadingAds()
            interstitialManager.showAdFullScreen(requireActivity(), onContinue)
        }, 1000)
    }

    fun showToast(contentId: Int, gravity: Int) {
        val toast = CustomToast.makeText(
            context = requireContext(),
            contentId = contentId,
            bgDrawable = R.drawable.bg_toast,
            gravity = gravity,
        )
        toast.show()
    }
}