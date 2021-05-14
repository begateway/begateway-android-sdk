package com.begateway.mobilepayments.models.googlepay.api

import com.begateway.mobilepayments.models.googlepay.android.response.PaymentMethodData
import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class GRequest(
    @SerializedName("apiVersion") val apiVersion: Int,
    @SerializedName("apiVersionMinor") val apiVersionMinor: Int,
    @SerializedName("paymentMethodData") val paymentMethodData: PaymentMethodData
) : AdditionalFields()