package com.wavez.base.ui.premium

class BillingConstant {
    companion object {
        fun isSub(productId: String): Boolean {
            return productId == WEEKLY_PREMIUM || productId == MONTHLY_PREMIUM || productId == YEAR_PREMIUM
        }

        fun isConsumeAble(productId: String): Boolean {
            if (isSub(productId)) {
                return false
            } else {
                throw IllegalArgumentException("Unknown product id: $productId")
            }
        }

        const val WEEKLY_PREMIUM = "weekly_pro"

        const val MONTHLY_PREMIUM = "monthly_pro"

        const val YEAR_PREMIUM = "yearly_trial"

        const val LIFETIME_IN_APP_PURCHASE = "remove_ads_lifetime"

        val subs = listOf(WEEKLY_PREMIUM, MONTHLY_PREMIUM, YEAR_PREMIUM)

        val inAppNonConsumes = listOf(LIFETIME_IN_APP_PURCHASE)

        val inAppConsumes = emptyList<String>()

    }
}