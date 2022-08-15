package com.begateway.mobilepayments.models.network

import com.google.gson.annotations.SerializedName

class CheckoutWithToken(
    @SerializedName("token") val token: String,
    @SerializedName("redirect_url") val redirectUrl: String? = null,
) : AdditionalFields()