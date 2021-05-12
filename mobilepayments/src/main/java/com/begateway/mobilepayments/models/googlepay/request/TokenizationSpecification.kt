package com.begateway.mobilepayments.models.googlepay.request

import com.begateway.mobilepayments.models.googlepay.TypeOfPaymentMethodTokenization
import com.google.gson.annotations.SerializedName

data class TokenizationSpecification(
        @SerializedName("type") val type: TypeOfPaymentMethodTokenization = TypeOfPaymentMethodTokenization.PAYMENT_GATEWAY,
        @SerializedName("parameters") val parameters: Parameters
)

