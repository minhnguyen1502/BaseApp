package com.wavez.base.ui.premium.repository

import android.app.Activity
import com.wavez.billing.BillingLibrary
import com.wavez.billing.callback.ConnectionListener
import com.wavez.billing.callback.OnPurchasesUpdatedListener
import com.wavez.billing.model.SaleProductDetail
import com.wavez.billing.model.SalePurchase
import com.wavez.base.evenbus.UpdateVip
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.premium.BillingConstant
import com.wavez.base.ui.premium.ResponseState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import org.greenrobot.eventbus.EventBus

class PremiumRepository(
    private val billingManager: BillingLibrary,
    private val sharedPrefData: SharedPref,
) : OnPurchasesUpdatedListener {

    init {
        addListener(this)
    }

    // Hoàn tất khi queryPurchases() lần đầu (bắn từ onConnectionSuccess) trả kết quả,
    // hoặc khi kết nối billing thất bại/mất kết nối (không có gì để chờ thêm).
    private val firstPurchaseCheck = CompletableDeferred<Unit>()

    // Chờ tối đa timeoutMs để lần kiểm tra purchase đầu tiên với Google Play hoàn tất,
    // đảm bảo sharedPrefData.isVip đã phản ánh trạng thái gói thật (hết hạn/huỷ) trước
    // khi bên gọi đọc isVip để quyết định điều hướng. Hết timeout mà billing chưa trả lời
    // (offline, mạng chậm) thì đành dùng giá trị cache hiện có.
    suspend fun awaitFirstPurchaseCheck(timeoutMs: Long) {
        withTimeoutOrNull(timeoutMs) { firstPurchaseCheck.await() }
    }

    private fun completeFirstPurchaseCheck() {
        if (!firstPurchaseCheck.isCompleted) {
            firstPurchaseCheck.complete(Unit)
        }
    }

    fun startConnection() {
        billingManager.startConnection(object : ConnectionListener {
            override fun onConnectionSuccess() {
                queryPurchases()
                querySaleProductDetails()
            }

            override fun onDisconnected() {
                _saleProductDetails.value = ResponseState.Error("Disconnected")
                completeFirstPurchaseCheck()
            }

            override fun onConnectionFailure() {
                _saleProductDetails.value = ResponseState.Error("Connection Failure")
                completeFirstPurchaseCheck()
            }
        })
    }


    private val _isPremium: MutableStateFlow<Boolean> = MutableStateFlow(sharedPrefData.isVip)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _saleProductDetails =
        MutableStateFlow<ResponseState<List<SaleProductDetail>>>(ResponseState.Loading)
    val saleProductDetails: StateFlow<ResponseState<List<SaleProductDetail>>> = _saleProductDetails

    private val _purchases =
        MutableStateFlow<ResponseState<List<SalePurchase>>>(ResponseState.Loading)

    fun addListener(ls: OnPurchasesUpdatedListener) {
        billingManager.addListener(ls)
    }

    fun removeListener(ls: OnPurchasesUpdatedListener) {
        billingManager.removeListener(ls)
    }

    private fun querySaleProductDetails() {
        _saleProductDetails.value = ResponseState.Loading
        billingManager.queryProductDetails(
            subs = BillingConstant.subs, inApp = listOf(), onCompleted = {
                _saleProductDetails.value = ResponseState.Success(it)
            })
    }

    private fun queryPurchases() {
        _purchases.value = ResponseState.Loading
        billingManager.queryPurchases(onCompleted = {
            unlockPremium(it.isNotEmpty())
            _purchases.value = ResponseState.Success(it)
            completeFirstPurchaseCheck()
        })
    }

    fun buy(activity: Activity, product: SaleProductDetail) {
        billingManager.purchase(activity, product)
    }

    override fun onSucceeded(purchase: SalePurchase) {
        val expiryMillis = System.currentTimeMillis() + when (purchase.productId) {
            BillingConstant.WEEKLY_PREMIUM -> 7L * 24 * 60 * 60 * 1000
            BillingConstant.MONTHLY_PREMIUM -> 30L * 24 * 60 * 60 * 1000
            BillingConstant.YEAR_PREMIUM -> 365L * 24 * 60 * 60 * 1000
            else -> 365L * 24 * 60 * 60 * 1000
        }
        sharedPrefData.vipExpiryDate = expiryMillis
        unlockPremium(true)
        billingManager.onTransactionCompleted(
            purchase, BillingConstant.isConsumeAble(purchase.productId)
        )
    }

    override fun onFailed(responseCode: Int) {
    }

    fun unlockPremium(pre: Boolean) {
        sharedPrefData.isVip = pre
        _isPremium.value = pre
        EventBus.getDefault().post(UpdateVip(pre))
    }


}
