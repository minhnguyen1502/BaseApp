package com.wavez.base.ui.premium

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.billing.callback.OnPurchasesUpdatedListener
import com.wavez.billing.getDailyPriceOnly
import com.wavez.billing.model.PurchaseResponseCode
import com.wavez.billing.model.SaleProductDetail
import com.wavez.billing.model.SalePurchase
import com.wavez.base.ui.premium.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: PremiumRepository
) : ViewModel(), OnPurchasesUpdatedListener {

    val isPremium: StateFlow<Boolean> = repository.isPremium

    val saleProductDetails = repository.saleProductDetails

    var currentProductSelected = BillingConstant.YEAR_PREMIUM

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Initial)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _year: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    private val _month: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    private val _week: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    init {
        repository.addListener(this)
        collectSaleProductDetails()
    }

    fun reConnect() {
        repository.startConnection()
    }

    fun unlockPremium(pre: Boolean) {
        repository.unlockPremium(pre)
    }

    private fun collectSaleProductDetails() {
        viewModelScope.launch {
            repository.saleProductDetails.collect { state ->
                when (state) {
                    is ResponseState.Success -> {
                        val saleProductDetails: List<SaleProductDetail> = state.data
                        _year.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.YEAR_PREMIUM })
                        _month.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.MONTHLY_PREMIUM })
                        _week.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.WEEKLY_PREMIUM })
                    }

                    is ResponseState.Error -> {
                        _year.value = ResponseState.Error(state.message)
                        _month.value = ResponseState.Error(state.message)
                        _week.value = ResponseState.Error(state.message)
                    }

                    is ResponseState.Loading -> {
                        _year.value = ResponseState.Loading
                        _month.value = ResponseState.Loading
                        _week.value = ResponseState.Loading

                    }
                }
            }
        }
    }

    fun buyPremium(activity: Activity) {
        Log.e("productDetail", "buyPremium ")
        val productDetail = when (currentProductSelected) {
            BillingConstant.YEAR_PREMIUM -> {
                _year.value
            }

            BillingConstant.MONTHLY_PREMIUM -> {
                _month.value
            }

            else -> {
                _week.value
            }
        }
        Log.d("productDetail", "buyPremium: $productDetail")
        if (productDetail is ResponseState.Success) {
            productDetail.data?.let {
                repository.buy(activity, it)
            }
        }

    }

    fun getPrices(saleProductDetail: SaleProductDetail): String {
        return if (BillingConstant.isSub(saleProductDetail.productId)) {
            val subscriptionOfferDetails = saleProductDetail.subscriptionOfferDetails
            subscriptionOfferDetails?.last()?.pricingPhases?.last()?.getReadAblePrice() ?: "unknown"
        } else {
            val oneTimePurchaseOfferDetails = saleProductDetail.oneTimePurchaseOfferDetails
            oneTimePurchaseOfferDetails?.getReadAblePrice() ?: "unknown"
        }
    }

    fun getFormattedPriceDivided(saleProductDetail: SaleProductDetail): String {
        return saleProductDetail.getDailyPriceOnly()
    }

    override fun onSucceeded(purchase: SalePurchase) {
        _purchaseState.value = PurchaseState.Success(purchase)
    }

    override fun onFailed(responseCode: Int) {
        if (responseCode == PurchaseResponseCode.USER_CANCELED) return
        _purchaseState.value = PurchaseState.Error(responseCode)
    }

    override fun onCleared() {
        repository.removeListener(this)
        super.onCleared()
    }

}