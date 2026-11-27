package com.wavez.base.ui.setting.dialogs

import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.RateDialogBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.linkAppStore
import com.wavez.base.extension.visible
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.setting.FeedbackActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class RateDialog : BaseDialogFragment<RateDialogBinding>() {
    companion object {
        const val TAG = "RateDialog"

        @JvmStatic
        fun newInstance() = RateDialog()
    }

    override fun bindingView() = RateDialogBinding.inflate(layoutInflater)

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.8f)
        }
    }

    override fun initListener() = with(binding) {
        super.initListener()

        rating.setOnRatingChangeListener { _, rating, _ ->
            when (rating) {
                5F -> {
                    Glide.with(this@RateDialog).load(R.drawable.ic_rate).into(icState)
                    title.text = getString(R.string.thank_you_for_using_iptv)
                    content.text = getString(R.string.content_rate_5)
                    btnFeedback.gone()
                    btnRate.visible()

                }

                4F -> {
                    Glide.with(this@RateDialog).load(R.drawable.ic_rate).into(icState)

                    title.text = getString(R.string.thank_you_for_using_iptv)

                    content.text = getString(R.string.content_rate_4)

                    btnFeedback.gone()
                    btnRate.visible()

                }

                3F -> {
                    Glide.with(this@RateDialog).load(R.drawable.ic_rate).into(icState)

                    title.text = getString(R.string.thank_you_for_using_iptv)

                    content.text = getString(R.string.content_rate_3)

                    btnFeedback.visible()
                    btnRate.gone()
                }

                2F -> {
                    Glide.with(this@RateDialog).load(R.drawable.ic_rate).into(icState)

                    title.text = getString(R.string.thank_you_for_using_iptv)

                    content.text = getString(R.string.content_rate_2)

                    btnFeedback.visible()
                    btnRate.gone()

                }

                else -> {
                    Glide.with(this@RateDialog).load(R.drawable.ic_rate).into(icState)

                    title.text = getString(R.string.thank_you_for_using_iptv)

                    content.text = getString(R.string.content_rate_1)

                    btnFeedback.visible()
                    btnRate.gone()

                }
            }
        }

        btnFeedback.setOnClickListener {
            requireActivity().startActivity(FeedbackActivity.newIntent(requireContext(), true))
            dismiss()
        }

        btnRate.setOnClickListener {
            sharedPref.hasUserRated = true
            requireActivity().linkAppStore()
            dismiss()
        }
    }
}