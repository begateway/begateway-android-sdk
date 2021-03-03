package com.begateway.mobilepayments.models.network.response

data class BeGatewayResponse(
    val status: ResponseStatus = ResponseStatus.ERROR,
    val message: String? = "undefined",
    internal val paymentToken: String? = null,
    internal val threeDSUrl: String? = null
)