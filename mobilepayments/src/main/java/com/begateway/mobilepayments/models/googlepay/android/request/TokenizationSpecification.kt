package com.begateway.mobilepayments.models.googlepay.android.request

import com.begateway.mobilepayments.models.googlepay.android.TypeOfPaymentMethodTokenization
import com.google.gson.annotations.SerializedName

data class TokenizationSpecification(
    @SerializedName("type") val type: TypeOfPaymentMethodTokenization = TypeOfPaymentMethodTokenization.PAYMENT_GATEWAY,
    @SerializedName("parameters") val parameters: Parameters
)

