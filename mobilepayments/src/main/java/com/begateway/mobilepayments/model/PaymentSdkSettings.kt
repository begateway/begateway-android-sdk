package com.begateway.mobilepayments.model

internal data class PaymentSdkSettings(
    var endpoint: String = "",
    var returnUrl: String? = null,
    var notificationUrl: String? = null,
    var transactionType: TransactionType? = null,
    var isTestMode: Boolean = false,
    var isDebugMode: Boolean = false,
    var publicKey: String? = null
)