package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

class Request(
    @SerializedName("token") val token: String,
    @SerializedName("payment_method") val paymentMethod: PaymentMethodType,
    @SerializedName("credit_card") val creditCard: CreditCard? = null,
    @SerializedName("encrypted_credit_card") internal val encryptedCreditCard: CreditCard? = null,
) : AdditionalFields()