package com.wavez.admob.native_ads.template

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.base.databinding.GntBigTemplateViewBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.invisible
import com.wavez.base.extension.visible
import com.google.android.gms.ads.nativead.NativeAd

class NativeTemplate @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NativeAdView {

    var binding: GntBigTemplateViewBinding
    override var isBound: Boolean = false

    private var _shouldShowAd = true
    override var shouldShowAd: Boolean
        get() = _shouldShowAd
        set(value) {
            _shouldShowAd = value
            if (_shouldShowAd) showPlaceholder() else hidePlaceholder()
        }

    init {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = GntBigTemplateViewBinding.inflate(layoutInflater)
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        addView(binding.root)
    }

    override fun bind(nativeAd: NativeAd) {
        try {
            if (!shouldShowAd) {
                hidePlaceholder()
                return
            } else {
                showPlaceholder()
            }
            binding.shimmerFrameLayout.isSelected = true
            binding.nativeAdView.visible()
            binding.adView.visible()
            binding.loadingLayout.gone()
            binding.btnCollapse.invisible()
            binding.tvAds.text = "Ad"
            binding.adHeadline.text = nativeAd.headline
            binding.nativeAdView.headlineView = binding.adHeadline

            if (nativeAd.callToAction == null) {
                binding.adCallToAction.visibility = GONE
            } else {
                binding.adCallToAction.visibility = VISIBLE
                binding.adCallToAction.text = nativeAd.callToAction
                binding.nativeAdView.callToActionView = binding.adCallToAction

            }
            val mediaContent = nativeAd.mediaContent
            if (mediaContent != null) {
                binding.adMedia.mediaContent = mediaContent
                binding.nativeAdView.mediaView = binding.adMedia
            }
            binding.nativeAdView.setNativeAd(nativeAd)
            binding.shimmerFrameLayout.isSelected = true
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.hideShimmer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showPlaceholder() {
        binding.nativeAdView.visible()
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        binding.loadingLayout.visible()
    }

    override fun hidePlaceholder() {
        Log.e("asdasd", "hidePlaceholder: ")
        binding.nativeAdView.gone()

    }

    fun performClickAd() {
        binding.nativeAdView.callToActionView?.callOnClick()
    }

    fun reset() {
        binding.loadingLayout.visible()
        binding.adView.invisible()
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.showShimmer(true)
    }
}