package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class PaymentMethodType {
    @SerializedName("credit_card") CREDIT_CARD,
    @SerializedName("erip") ERIP,
}