package com.begateway.mobilepayments.models.network

import com.google.gson.annotations.SerializedName

data class GooglePay(
    @SerializedName("gateway_id") val gateway_id: String,
    @SerializedName("gateway_merchant_id") val gateway_merchant_id: String,
    @SerializedName("status") val status: String
) : AdditionalFields()