package com.begateway.mobilepayments.models.googlepay.request

import com.google.gson.annotations.SerializedName

data class PaymentTypeParameters(
        @SerializedName("allowedAuthMethods") val allowedAuthMethods: List<String>,
        @SerializedName("allowedCardNetworks") val allowedCardNetworks: List<String>
)
