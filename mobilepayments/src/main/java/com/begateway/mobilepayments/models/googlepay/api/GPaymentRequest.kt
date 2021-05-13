package com.begateway.mobilepayments.models.googlepay.api

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class GPaymentRequest(
    @SerializedName("token") val token: String,
    @SerializedName("request") val request: GRequest,
    @SerializedName("contract") val contract: Boolean = false
) : AdditionalFields()