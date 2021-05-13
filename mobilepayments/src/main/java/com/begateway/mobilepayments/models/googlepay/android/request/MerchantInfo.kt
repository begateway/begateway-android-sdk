package com.begateway.mobilepayments.models.googlepay.android.request

import com.google.gson.annotations.SerializedName

data class MerchantInfo(
		@SerializedName("merchantName") val merchantName: String
)