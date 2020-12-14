package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

enum class PaymentMethodType {
    @SerializedName("credit_card") CREDIT_CARD,
    @SerializedName("erip") ERIP,
}