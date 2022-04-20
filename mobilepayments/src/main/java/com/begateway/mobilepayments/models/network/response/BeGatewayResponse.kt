package com.begateway.mobilepayments.models.network.response

import androidx.annotation.Keep

@Keep
data class BeGatewayResponse(
    val status: ResponseStatus = ResponseStatus.ERROR,
    val message: String? = "undefined",
    internal val paymentToken: String? = null,
    internal val threeDSUrl: String? = null,
    internal val resultUrl: String? = null
)