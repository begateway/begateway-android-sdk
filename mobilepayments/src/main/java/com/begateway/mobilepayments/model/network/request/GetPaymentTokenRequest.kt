package com.begateway.mobilepayments.model.network.request

import com.google.gson.annotations.SerializedName

data class GetPaymentTokenRequest(
    @SerializedName("checkout") val checkout: Checkout
)