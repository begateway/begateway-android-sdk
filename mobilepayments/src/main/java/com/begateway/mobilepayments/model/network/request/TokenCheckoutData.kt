package com.begateway.mobilepayments.model.network.request

import com.begateway.mobilepayments.model.Checkout
import com.google.gson.annotations.SerializedName

data class TokenCheckoutData(
    @SerializedName("checkout") val checkout: Checkout
)