package com.begateway.mobilepayments.model

internal data class PaymentSdkSettings(
    var endpoint: String = "",
    var returnUrl: String = "https://DEFAULT_RETURN_URL.com",
    var notificationUrl: String? = null,
    var transactionType: TransactionType? = null,
    var isDebugMode: Boolean = false,
    var publicKey: String = ""
)