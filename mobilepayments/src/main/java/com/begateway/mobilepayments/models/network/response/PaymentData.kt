package com.begateway.mobilepayments.models.network.response

import com.begateway.mobilepayments.models.network.GooglePay
import com.begateway.mobilepayments.models.network.request.Order
import com.begateway.mobilepayments.models.network.request.Settings
import com.google.gson.annotations.SerializedName

data class PaymentData(
    @SerializedName("checkout") val checkout: PaymentCheckout,
)

data class PaymentCheckout(
    @SerializedName("token") val token: String,
    @SerializedName("order") val order: Order? = null,
    @SerializedName("settings") val settings: Settings? = null,
    @SerializedName("status") private val _status: ResponseStatus? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("google_pay") val googlePay: GooglePay? = null,
) {
    val status: ResponseStatus
        get() = _status ?: ResponseStatus.ERROR
}