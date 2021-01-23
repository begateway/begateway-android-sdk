package com.begateway.mobilepayments.model.network.request

import com.begateway.mobilepayments.model.Request
import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("request") val request: Request
)