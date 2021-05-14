package com.begateway.mobilepayments.models.googlepay.android.request

import com.google.gson.annotations.SerializedName


data class AllowedPaymentMethods(
    @SerializedName("type") val type: TypeOfPaymentMethod = TypeOfPaymentMethod.CARD,
    @SerializedName("parameters") val parameters: PaymentTypeParameters,
    @SerializedName("tokenizationSpecification") val tokenizationSpecification: TokenizationSpecification?
)

enum class TypeOfPaymentMethod {
    @SerializedName("CARD")
    CARD,

    @SerializedName("PAYPAL")
    PAYPAL;
}