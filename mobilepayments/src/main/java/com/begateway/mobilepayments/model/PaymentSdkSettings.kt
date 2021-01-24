package com.begateway.mobilepayments.model

internal data class PaymentSdkSettings(
    var endpoint: String = "",
    var isDebugMode: Boolean = false,
    var publicKey: String = "",
    var returnUrl: String = ""
)