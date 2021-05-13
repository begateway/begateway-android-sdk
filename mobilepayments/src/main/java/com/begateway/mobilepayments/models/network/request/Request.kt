package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

data class Request(
    @SerializedName("token") val token: String,
    @SerializedName("payment_method") val paymentMethod: PaymentMethodType,
    @SerializedName("credit_card") val creditCard: CreditCard? = null
) : AdditionalFields()