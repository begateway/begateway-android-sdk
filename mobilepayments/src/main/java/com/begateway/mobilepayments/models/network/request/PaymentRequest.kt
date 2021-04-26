package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("request") val request: Request
) : AdditionalFields()