package com.begateway.mobilepayments.models.googlepay.android.response

import com.begateway.mobilepayments.models.googlepay.android.TypeOfPaymentMethodTokenization
import com.google.gson.annotations.SerializedName

data class TokenizationData(
    @SerializedName("type") val type: TypeOfPaymentMethodTokenization,
    @SerializedName("token") val token: String
)