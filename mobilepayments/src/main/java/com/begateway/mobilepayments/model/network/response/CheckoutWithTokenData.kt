package com.begateway.mobilepayments.model.network.response

import com.begateway.mobilepayments.model.CheckoutWithToken
import com.google.gson.annotations.SerializedName

data class CheckoutWithTokenData(
    @SerializedName("checkout") val checkout: CheckoutWithToken
)