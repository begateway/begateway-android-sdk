package com.begateway.mobilepayments.models.googlepay.request

import com.google.gson.annotations.SerializedName

data class Parameters (
		@SerializedName("gateway") val gateway : String,
		@SerializedName("gatewayMerchantId") val gatewayMerchantId : String
)