package com.begateway.mobilepayments

import com.begateway.mobilepayments.model.PaymentSdkSettings
import com.begateway.mobilepayments.model.TransactionType

class PaymentSdkBuilder {
    private val settings = PaymentSdkSettings()

    fun setEndpoint(endpoint: String) = apply { settings.endpoint = endpoint }
    fun setReturnUrl(returnUrl: String) = apply { settings.returnUrl = returnUrl }
    fun setNotificationUrl(notificationUrl: String) =
        apply { settings.notificationUrl = notificationUrl }

    fun setTransactionType(transactionType: TransactionType) =
        apply { settings.transactionType = transactionType }

    fun setDebugMode() = apply { settings.isDebugMode = true }
    fun setPublicKey(publicKey: String) = apply { settings.publicKey = publicKey }

    @Throws(IllegalArgumentException::class)
    fun build(): PaymentSdk {
        require(settings.publicKey.isNotBlank()) { "Public key can not be empty" }
        require(settings.endpoint.isNotBlank()) { "Endpoint can not be empty" }
        return PaymentSdk.instance.apply { applySettings(settings) }
    }
}