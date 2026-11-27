package com.wavez.billing.callback

import com.wavez.billing.model.SalePurchase

interface OnPurchasesUpdatedListener {

    fun onSucceeded(purchase: SalePurchase)

    fun onFailed(responseCode: Int)

}