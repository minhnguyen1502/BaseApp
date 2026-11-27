package com.wavez.billing

import android.app.Activity
import com.wavez.billing.callback.ConnectionListener
import com.wavez.billing.callback.OnPurchasesUpdatedListener
import com.wavez.billing.model.SaleProductDetail
import com.wavez.billing.model.SalePurchase
import com.wavez.billing.model.SubscriptionOfferDetail

interface BillingLibrary {

    fun startConnection(listener: ConnectionListener)

    fun queryProductDetails(
        subs: List<String>,
        inApp: List<String>,
        onCompleted: (List<SaleProductDetail>) -> Unit
    )

    fun queryPurchases(onCompleted: (List<SalePurchase>) -> Unit)

    fun addListener(listener: OnPurchasesUpdatedListener)

    fun removeListener(listener: OnPurchasesUpdatedListener)

    fun onTransactionCompleted(salePurchase: SalePurchase, isConsume: Boolean)

    fun purchase(activity: Activity, saleProductDetail: SaleProductDetail)

    fun purchase(
        activity: Activity,
        saleProductDetail: SaleProductDetail,
        subscriptionOfferDetail: SubscriptionOfferDetail
    )

    fun endConnection()

}