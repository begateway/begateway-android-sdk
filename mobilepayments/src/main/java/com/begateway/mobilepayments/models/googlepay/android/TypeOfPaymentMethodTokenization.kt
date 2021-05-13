package com.begateway.mobilepayments.models.googlepay.android

import com.google.gson.annotations.SerializedName

enum class TypeOfPaymentMethodTokenization {
    @SerializedName("PAYMENT_GATEWAY")
    PAYMENT_GATEWAY,

    @SerializedName("DIRECT")
    DIRECT;
}