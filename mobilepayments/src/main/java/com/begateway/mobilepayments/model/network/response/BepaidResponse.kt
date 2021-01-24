package com.begateway.mobilepayments.model.network.response

data class BepaidResponse(
    val status: ResponseStatus = ResponseStatus.ERROR,
    val message: String? = "undefined",
    internal val paymentToken: String? = null,
    internal val threeDSUrl: String? = null
)