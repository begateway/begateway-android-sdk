package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

data class TokenCheckoutData(
    @SerializedName("checkout") val checkout: Checkout
)