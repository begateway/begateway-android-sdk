package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class SamsungPayTokenRequest(
    @SerializedName("request") val request: SamsungPayRequest,
) : AdditionalFields()

data class SamsungPayRequest(
    @SerializedName("token") val token: String,
    @SerializedName("ref_id") val refId: String? = null
) : AdditionalFields()