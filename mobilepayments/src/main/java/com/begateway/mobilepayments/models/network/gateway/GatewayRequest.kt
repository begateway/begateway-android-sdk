package com.begateway.mobilepayments.models.network.gateway

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.begateway.mobilepayments.models.network.request.CreditCard
import com.begateway.mobilepayments.models.network.request.Customer
import com.google.gson.annotations.SerializedName

data class GatewayRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String,
    @SerializedName("tracking_id") val tracking_id: String,
    @SerializedName("test") val test: Boolean = false,
    @SerializedName("credit_card") val creditCard: CreditCard? = null,
    @SerializedName("customer") val customer: Customer? = null
) : AdditionalFields()