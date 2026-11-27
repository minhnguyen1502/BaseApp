package com.wavez.base.ui.premium

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityPremiumBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.premium.dialogs.PurchaseFailedDialog
import com.wavez.base.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumActivity : BaseActivity<ActivityPremiumBinding>(),
    PurchaseFailedDialog.PurchaseFailedListener {
    companion object {
        const val IS_FROM_SPLASH = "IS_FROM_SPLASH"
        const val TAG = "adadadad"

        fun newIntent(context: Context, isFormSplash: Boolean = false) =
            Intent(context, PremiumActivity::class.java).apply {
                putExtra(IS_FROM_SPLASH, isFormSplash)

            }
    }

    override fun bindingView() = ActivityPremiumBinding.inflate(layoutInflater)
    private var isFormSplash = false

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        isFormSplash = intent.getBooleanExtra(IS_FROM_SPLASH, false)
        syncUiSelect(BillingConstant.WEEKLY_PREMIUM)
        Glide.with(this).load(R.drawable.img_premium).centerCrop().into(binding.premiumBg)

    }

    override fun initListener() = with(binding) {
        super.initListener()

        btnWeekly.setOnClickListener {
            binding.btnPurchase.text = getString(R.string._continue)
            syncUiSelect(BillingConstant.WEEKLY_PREMIUM)
        }
        btnYearly.setOnClickListener {
            binding.btnPurchase.text = getString(R.string._continue)
            syncUiSelect(BillingConstant.YEAR_PREMIUM)
        }

        btnMonthly.setOnClickListener {
            binding.btnPurchase.text = getString(R.string._continue)
            syncUiSelect(BillingConstant.MONTHLY_PREMIUM)
        }

        btnCancel.setOnClickListener {
            nextScreen()
        }

        btnClose.setOnClickListener {
            nextScreen()
        }

        btnTerm.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW, Constant.TERM_AND_SERVICE.toUri()
            )
            try {
                startActivity(browserIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        btnPolicy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW, Constant.PRIVACY_POLICY_LINK.toUri()
            )
            try {
                startActivity(browserIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        btnPurchase.setOnClickListener {
            Log.e("productDetail", "buy ")
            premiumViewModel.buyPremium(this@PremiumActivity)
        }
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle STARTED")

                premiumViewModel.saleProductDetails.collect { state ->
                    Log.d(TAG, "collect state = ${state::class.java.simpleName}")

                    when (state) {
                        is ResponseState.Loading -> {
                            Log.d(TAG, "Loading product details")
                        }

                        is ResponseState.Error -> {
                            Log.e(TAG, "Error loading products ${state.message}")
                            binding.layoutBuy.gone()
                            binding.layoutError.visible()
                            binding.btnPurchase.text = getString(R.string.try_again)
                        }

                        is ResponseState.Success -> {
                            Log.d(TAG, "Success - product size = ${state.data.size}")

                            if (state.data.isEmpty()) {
                                Log.w(TAG, "Product list is EMPTY")
                                binding.layoutBuy.gone()
                                binding.layoutError.visible()
                                binding.btnPurchase.text = getString(R.string.try_again)
                            } else {
                                binding.layoutBuy.visible()
                                binding.layoutError.gone()

                                state.data.forEach {
                                    Log.d(
                                        TAG, "ProductId=${it.productId}, price=${
                                            premiumViewModel.getPrices(it)
                                        }"
                                    )
                                }

                                state.data.find { it.productId == BillingConstant.WEEKLY_PREMIUM }
                                    ?.let {
                                        val price = premiumViewModel.getPrices(it)
                                        Log.d(TAG, "Weekly price = $price")
                                        binding.priceWeekly.text = price
                                    }

                                state.data.find { it.productId == BillingConstant.MONTHLY_PREMIUM }
                                    ?.let {
                                        val price = premiumViewModel.getPrices(it)
                                        Log.d(TAG, "Monthly price = $price")
                                        binding.priceMonthly.text = price
                                    }

                                state.data.find { it.productId == BillingConstant.YEAR_PREMIUM }
                                    ?.let {
                                        val price = premiumViewModel.getPrices(it)
                                        Log.d(TAG, "Yearly price = $price")
//                                        val fullText = price
//                                        val spannable = SpannableString(fullText)
//
//                                        val start = fullText.indexOf(price)
//                                        val end = start + price.length
//
//                                        if (start >= 0) {
//                                            spannable.setSpan(
//                                                StyleSpan(Typeface.BOLD),
//                                                start,
//                                                end,
//                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                                            )
//                                        }

                                        binding.priceYearly.text = price
                                    }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                premiumViewModel.purchaseState.collect { state ->
                    when (state) {
                        is PurchaseState.Error -> {
                            PurchaseFailedDialog.newInstance().show(
                                supportFragmentManager, PurchaseFailedDialog::class.java.simpleName
                            )
                        }

                        PurchaseState.Initial -> {
                        }

                        is PurchaseState.Success -> {
                            startActivity(HomeActivity.newIntent(this@PremiumActivity, showPurchaseSuccess = true))
                            finishAffinity()
                        }
                    }
                }
            }
        }
    }

    private fun syncUiSelect(type: String) {
        premiumViewModel.currentProductSelected = type
        binding.btnWeekly.isSelected = type == BillingConstant.WEEKLY_PREMIUM
        binding.btnYearly.isSelected = type == BillingConstant.YEAR_PREMIUM
        binding.btnMonthly.isSelected = type == BillingConstant.MONTHLY_PREMIUM

    }

    private fun nextScreen() {
        if (isFormSplash) {
            startActivity(HomeActivity.newIntent(this))
        }
        finish()
    }

    override fun onHandleBackPressed() {
        super.onHandleBackPressed()
        nextScreen()
    }

    override fun onTryAgain() {
        premiumViewModel.reConnect()
    }
}