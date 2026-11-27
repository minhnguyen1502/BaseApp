package com.wavez.billing

import android.annotation.SuppressLint
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.wavez.billing.model.SaleProductDetail

fun ProductDetails.getFormattedPrice(
    subscriptionBasePlanId: String? = null,
    subscriptionOfferId: String? = null,
    subscriptionPricingPhaseIndex: Int = 0,
): String? {
    return if (isInAppPurchase()) {
        oneTimePurchaseOfferDetails?.formattedPrice
    } else {
        try {
            if (isSubscription()) {
                // Find the first matching offer based on the base plan ID and offer ID, or default to the first
                val offer = subscriptionOfferDetails?.firstOrNull { offer ->
                    (subscriptionBasePlanId == null || offer.basePlanId == subscriptionBasePlanId) &&
                            (subscriptionOfferId == null || offer.offerId == subscriptionOfferId)
                }
                return offer?.pricingPhases?.pricingPhaseList?.getOrNull(
                    subscriptionPricingPhaseIndex
                )?.formattedPrice
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}


@SuppressLint("DefaultLocale")
fun ProductDetails.getFormattedPriceDivided(
    subscriptionBasePlanId: String? = null,
    subscriptionOfferId: String? = null,
    subscriptionPricingPhaseIndex: Int = 0,
    divider: Int,
    dividerFormat: String = "%.2f",
): String? {
    return getFormattedPrice(
        subscriptionBasePlanId,
        subscriptionOfferId,
        subscriptionPricingPhaseIndex,
    )?.let { price ->
        // Regex to match currency before or after the numeric part, with comma or dot as decimal separator
        val regex = """(\D*)\s*([\d,\.]+)\s*(\D*)""".toRegex()
        val matchResult = regex.matchEntire(price)

        return if (matchResult != null) {
            val (currencyPrefix, numericPart, currencySuffix) = matchResult.destructured

            // Clean numeric part by removing commas and replacing dot as decimal separator
            val normalizedNumber = numericPart.replace(",", "").toDoubleOrNull()
            if (normalizedNumber != null) {
                // Divide and format the result with the given decimal format
                val dividedPrice = normalizedNumber / divider
                val formattedPrice = String.format(dividerFormat, dividedPrice)

                // Construct the new price string with currency in the original position
                "${currencyPrefix.trim()}$formattedPrice${currencySuffix.trim()}"
            } else {
                null // Return null if numeric conversion fails
            }
        } else {
            null // Return null if the price format is not as expected
        }
    }
}

@SuppressLint("DefaultLocale")
fun SaleProductDetail.getDailyPriceOnly(
    dividerFormat: String = "%.2f"
): String {
    val formattedPrice = when {
        subscriptionOfferDetails?.isNotEmpty() == true -> {
            val offer = subscriptionOfferDetails.last()
            val phase = offer.pricingPhases.lastOrNull()
            phase?.formattedPrice ?: return "unknown"
        }
        oneTimePurchaseOfferDetails != null -> {
            oneTimePurchaseOfferDetails.getReadAblePrice()
        }
        else -> return "unknown"
    }

    val regex = """(\D*)\s*([\d,\.]+)\s*(\D*)""".toRegex()
    val matchResult = regex.matchEntire(formattedPrice) ?: return formattedPrice

    val (prefix, numeric, suffix) = matchResult.destructured
    val number = numeric.replace(",", "").toDoubleOrNull() ?: return formattedPrice

    // Xác định số ngày theo chu kỳ
    val divider = when {
        productId.contains("year", true) -> 365.0
        productId.contains("month", true) -> 30.0
        else -> 1.0
    }

    val dailyPrice = number / divider
    return "${prefix.trim()}${String.format(dividerFormat, dailyPrice)}${suffix.trim()}"
}


fun ProductDetails.isInAppPurchase(): Boolean = productType == BillingClient.ProductType.INAPP

fun ProductDetails.isSubscription(): Boolean = productType == BillingClient.ProductType.SUBS
