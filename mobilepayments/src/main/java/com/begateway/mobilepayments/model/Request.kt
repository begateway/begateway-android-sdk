package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

class Request(
    @SerializedName("token") val token: String,
    @SerializedName("payment_method") val paymentMethod: PaymentMethodType,
    @SerializedName("credit_card") val creditCard: CreditCard,
) : AdditionalFields()