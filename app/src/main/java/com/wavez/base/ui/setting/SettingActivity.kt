package com.wavez.base.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivitySettingBinding
import com.wavez.base.evenbus.UpdateVip
import com.wavez.base.extension.gone
import com.wavez.base.ui.language.LanguageActivity
import com.wavez.base.ui.premium.PremiumActivity
import com.wavez.base.ui.premium.dialogs.PurchaseSuccessDialog
import com.wavez.base.ui.setting.dialogs.RateDialog
import com.wavez.base.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    companion object {
        fun newIntent(context: Context) = Intent(context, SettingActivity::class.java)
    }

    override fun bindingView() = ActivitySettingBinding.inflate(layoutInflater)

    override val registerEventBus: Boolean
        get() = true

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        binding.toolbar.btnSave.gone()
        binding.toolbar.tvTitle.text = getString(R.string.setting)
        binding.btnRate.visibility = if (sharedPref.hasUserRated) View.GONE else View.VISIBLE

        setUi(sharedPref.isVip)
    }

    override fun initListener() = with(binding) {
        super.initListener()

        toolbar.btnBack.setOnClickListener {
            onBackWithAds {
                finish()
            }
        }

        btnLanguage.setOnClickListener {
            startActivity(LanguageActivity.newIntent(this@SettingActivity))
        }

        btnRate.setOnClickListener {
            RateDialog.newInstance().show(supportFragmentManager, RateDialog::class.java.simpleName)
        }

        btnFeedback.setOnClickListener {
            startActivity(FeedbackActivity.newIntent(this@SettingActivity))
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
            startActivity(PremiumActivity.newIntent(this@SettingActivity))
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
        if (event.isVip) {
            PurchaseSuccessDialog.newInstance().show(
                supportFragmentManager, PurchaseSuccessDialog::class.java.simpleName
            )
        }
    }
}