package com.wavez.admob.native_ads.template

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.base.databinding.NativeSmallTemplateBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.invisible
import com.wavez.base.extension.visible
import com.google.android.gms.ads.nativead.NativeAd

class NativeSmallTemplate @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NativeAdView {

    var binding: NativeSmallTemplateBinding
    override var isBound: Boolean = false

    init {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = NativeSmallTemplateBinding.inflate(layoutInflater)
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        addView(binding.root)
    }

    private var _shouldShowAd = true
    override var shouldShowAd: Boolean
        get() = _shouldShowAd
        set(value) {
            _shouldShowAd = value
            if (_shouldShowAd) showPlaceholder() else hidePlaceholder()
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
            binding.loadingLayout.gone()
            binding.tvAds.text = "Ad"
            binding.headlineView.text = nativeAd.headline
            binding.nativeAdView.headlineView = binding.headlineView

            if (nativeAd.callToAction == null) {
                binding.adCallToAction.visibility = GONE
            } else {
                binding.adCallToAction.visibility = VISIBLE
                binding.adCallToAction.text = nativeAd.callToAction
                binding.nativeAdView.callToActionView = binding.adCallToAction
            }
//            val mediaContent = nativeAd.mediaContent
//            if (mediaContent != null) {
//                binding.adMedia.mediaContent = mediaContent
//                binding.nativeAdView.mediaView = binding.adMedia
//            }

            binding.bodyView.text = nativeAd.body
            binding.nativeAdView.bodyView = binding.bodyView

            val nativeIcon = nativeAd.icon
            if (nativeIcon == null) {
                binding.iconView.gone()
                binding.cvIcon.gone()
            } else {
                binding.iconView.setImageDrawable(nativeIcon.drawable)
                binding.nativeAdView.iconView = binding.iconView
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
        binding.nativeAdView.gone()
    }

    fun reset() {
        binding.loadingLayout.visible()
        binding.nativeAdView.invisible()
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.showShimmer(true)
    }
}