package com.wavez.admob.native_ads.template

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wavez.admob.native_ads.NativeAdView
import com.wavez.base.databinding.GntBigTemplateViewBinding
import com.wavez.base.databinding.TemplateNativeCollapseBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.invisible
import com.wavez.base.extension.visible
import com.google.android.gms.ads.nativead.NativeAd
import kotlin.random.Random

class NativeCollapseTemplate @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NativeAdView {

    var binding: TemplateNativeCollapseBinding
    private var onCollapseListener: (() -> Unit)? = null
    // Dùng cho BaseActivity: user đóng ad → hẹn giờ load ad mới
    private var onClosedListener: (() -> Unit)? = null

    override var isBound: Boolean = false

    private var _shouldShowAd = true
    override var shouldShowAd: Boolean
        get() = _shouldShowAd
        set(value) {
            _shouldShowAd = value
            if (!_shouldShowAd) hidePlaceholder()
        }

    init {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = TemplateNativeCollapseBinding.inflate(layoutInflater)
        addView(binding.root)
        // Load ngầm: ẩn cho tới khi bind được ad
        binding.nativeAdView.gone()
        binding.btnCollapseType1.setOnClickListener { onCollapseClick() }
        binding.btnCollapseType2.setOnClickListener { onCollapseClick() }
    }

    fun setOnCollapseListener(listener: (() -> Unit)?) {
        onCollapseListener = listener
    }

    fun setOnClosedListener(listener: (() -> Unit)?) {
        onClosedListener = listener
    }

    private fun onCollapseClick() {
        isBound = false
        onCollapseListener?.invoke()
        onClosedListener?.invoke()
    }

    override fun bind(nativeAd: NativeAd) {
        try {
            if (!shouldShowAd) {
                hidePlaceholder()
                return
            }
            binding.shimmerFrameLayout.isSelected = true
            binding.nativeAdView.visible()
            binding.adView.visible()
            binding.loadingLayout.gone()
            randomizeCollapsePosition()
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

            val nativeIcon = nativeAd.icon
            if (nativeIcon == null) {
                binding.llIcon.gone()
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

            val mediaContent = nativeAd.mediaContent
            if (mediaContent != null) {
                binding.adMedia.mediaContent = mediaContent
                binding.nativeAdView.mediaView = binding.adMedia
            }
            binding.nativeAdView.setNativeAd(nativeAd)
            isBound = true
            binding.shimmerFrameLayout.isSelected = true
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.hideShimmer()
            // Load được → hiện luôn (kể cả khi trước đó user đã collapse)
            visible()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun randomizeCollapsePosition() {
        if (Random.nextBoolean()) {
            binding.btnCollapseType1.visible()
            binding.btnCollapseType2.invisible()
        } else {
            binding.btnCollapseType1.gone()
            binding.btnCollapseType2.visible()
        }
    }

    override fun showPlaceholder() {
        binding.nativeAdView.visible()
        binding.adView.invisible()
        binding.loadingLayout.visible()
        binding.shimmerFrameLayout.isSelected = false
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.showShimmer(true)
    }

    override fun hidePlaceholder() {
        Log.e("asdasd", "hidePlaceholder: ")
        isBound = false
        binding.nativeAdView.gone()

    }

    fun performClickAd() {
        binding.nativeAdView.callToActionView?.callOnClick()
    }

    fun reset() {
        isBound = false
    }
}