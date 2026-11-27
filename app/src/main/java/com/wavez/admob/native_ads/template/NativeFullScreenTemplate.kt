package com.wavez.admob.native_ads.template

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.base.databinding.FullScreenTemplateViewBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.google.android.gms.ads.nativead.NativeAd

class NativeFullScreenTemplate @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NativeAdView {
    private var binding: FullScreenTemplateViewBinding

    init {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FullScreenTemplateViewBinding.inflate(layoutInflater)
        addView(
            binding.root, LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            )
        )
    }

    override var shouldShowAd: Boolean = true
    override var isBound: Boolean = false


    override fun bind(nativeAd: NativeAd) {
        try {
            val mediaContext = nativeAd.mediaContent
            if (mediaContext != null) {
                binding.mediaView.mediaContent = mediaContext
                binding.nativeAdView.mediaView = binding.mediaView
                binding.mediaView.visible()
            } else {
                binding.mediaView.gone()
            }

            val callToAction = nativeAd.callToAction
            if (callToAction != null) {
                binding.adCallToAction.text = nativeAd.callToAction
                binding.nativeAdView.callToActionView = binding.adCallToAction
                binding.adCallToAction.visible()
            } else {
                binding.adCallToAction.gone()
            }

            val headline = nativeAd.headline
            if (headline != null) {
                binding.headlineView.text = nativeAd.headline
                binding.nativeAdView.headlineView = binding.headlineView
                binding.headlineView.visible()
            } else {
                binding.headlineView.gone()
            }

            val nativeIcon = nativeAd.icon
            if (nativeIcon == null) {
                binding.iconView.gone()
            } else {
                binding.iconView.setImageDrawable(nativeIcon.drawable)
                binding.nativeAdView.iconView = binding.iconView
            }

            val body = nativeAd.body
            if (body != null) {
                binding.bodyView.text = nativeAd.body
                binding.nativeAdView.bodyView = binding.bodyView
                binding.bodyView.visible()
            } else {
                binding.bodyView.gone()
            }

            binding.nativeAdView.setNativeAd(nativeAd)
            binding.shimmerFrameLayout.isSelected = true
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.hideShimmer()
        } catch (ex: Exception) {
            ex.printStackTrace()
            gone()
        }
    }

    override fun showPlaceholder() {
        binding.nativeAdView.visible()
        binding.shimmerFrameLayout.isSelected = true
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun hidePlaceholder() {
        binding.nativeAdView.gone()
    }
    fun performClickAd() {
        binding.nativeAdView.callToActionView?.callOnClick()
    }
}