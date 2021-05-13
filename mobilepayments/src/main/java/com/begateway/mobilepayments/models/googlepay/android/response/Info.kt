package com.begateway.mobilepayments.models.googlepay.android.response

import com.google.gson.annotations.SerializedName

data class Info(
        @SerializedName("cardNetwork") val cardNetwork: String,
        @SerializedName("cardDetails") val cardDetails: String
)