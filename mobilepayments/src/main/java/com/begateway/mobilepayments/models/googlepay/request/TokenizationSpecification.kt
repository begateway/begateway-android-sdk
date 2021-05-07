package com.begateway.mobilepayments.models.googlepay.request

import com.google.gson.annotations.SerializedName
import group.flowbird.mpp.model.googlePay.TypeOfPaymentMethodTokenization

data class TokenizationSpecification(
        @SerializedName("type") val type: TypeOfPaymentMethodTokenization = TypeOfPaymentMethodTokenization.PAYMENT_GATEWAY,
        @SerializedName("parameters") val parameters: Parameters
)

