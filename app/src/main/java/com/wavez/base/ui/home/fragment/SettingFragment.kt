package com.wavez.base.ui.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseFragment
import com.wavez.base.databinding.ActivitySettingBinding
import com.wavez.base.evenbus.UpdateVip
import com.wavez.base.ui.language.LanguageActivity
import com.wavez.base.ui.premium.PremiumActivity
import com.wavez.base.ui.setting.FeedbackActivity
import com.wavez.base.ui.setting.dialogs.RateDialog
import com.wavez.base.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SettingFragment : BaseFragment<ActivitySettingBinding>() {
    override fun bindingView(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override val registerEventBus: Boolean
        get() = true

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.btnRate.visibility = if (sharedPref.hasUserRated) View.GONE else View.VISIBLE

        setUi(sharedPref.isVip)
    }


    override fun initListener() = with(binding) {
        super.initListener()

        btnLanguage.setOnClickListener {
            startActivity(LanguageActivity.newIntent(requireContext()))
        }

        btnRate.setOnClickListener {
            RateDialog.newInstance().show(childFragmentManager, RateDialog::class.java.simpleName)
        }

        btnFeedback.setOnClickListener {
            startActivity(FeedbackActivity.newIntent(requireContext()))
        }

        btnPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW, Constant.PRIVACY_POLICY_LINK.toUri()
            )
            try {
                startActivity(browserIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        binding.btnUnlock.setOnClickListener {
            startActivity(PremiumActivity.newIntent(requireContext()))
        }
    }

    private fun setUi(isVip: Boolean) {
        if (isVip) {
            binding.layoutUnlock.visibility = View.GONE
            binding.layoutUnlocked.visibility = View.VISIBLE
            val expiryDate = sharedPref.vipExpiryDate
            if (expiryDate > 0L) {
                val formatted =
                    SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date(expiryDate))
                binding.tvTimeActive.text = getString(R.string.active_until, formatted)
            }
            Glide.with(this).load(R.drawable.img_pre_unlocked).centerCrop().into(binding.bgPre)
        } else {
            binding.layoutUnlock.visibility = View.VISIBLE
            binding.layoutUnlocked.visibility = View.GONE
            Glide.with(this).load(R.drawable.img_pre_unlock).centerCrop().into(binding.bgPre)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateVip(event: UpdateVip) {
        setUi(event.isVip)
    }
}