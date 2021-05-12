package com.begateway.mobilepayments.models.network.gateway

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class GatewayPaymentRequest(
    @SerializedName("request") val request: GatewayRequest
) : AdditionalFields()