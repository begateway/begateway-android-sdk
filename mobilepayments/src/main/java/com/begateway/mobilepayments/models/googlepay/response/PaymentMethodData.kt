package com.begateway.mobilepayments.models.googlepay.response

import com.google.gson.annotations.SerializedName

data class PaymentMethodData(
		@SerializedName("type") val type: String,
		@SerializedName("description") val description: String,
		@SerializedName("info") val info: Info,
		@SerializedName("tokenizationData") val tokenizationData: TokenizationData
)