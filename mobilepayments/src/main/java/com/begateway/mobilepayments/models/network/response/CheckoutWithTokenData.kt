package com.begateway.mobilepayments.models.network.response

import com.begateway.mobilepayments.models.network.CheckoutWithToken
import com.google.gson.annotations.SerializedName

data class CheckoutWithTokenData(
    @SerializedName("checkout") val checkout: CheckoutWithToken
)