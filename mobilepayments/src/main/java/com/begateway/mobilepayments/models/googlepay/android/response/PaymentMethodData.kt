package com.begateway.mobilepayments.models.googlepay.android.response

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class PaymentMethodData(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("info") val info: Info,
    @SerializedName("tokenizationData") val tokenizationData: TokenizationData
) : AdditionalFields()