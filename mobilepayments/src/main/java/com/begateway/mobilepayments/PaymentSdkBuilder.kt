package com.begateway.mobilepayments

import com.begateway.mobilepayments.model.PaymentSdkSettings
import com.begateway.mobilepayments.model.TransactionType

class PaymentSdkBuilder {
    private val settings = PaymentSdkSettings()

    fun setEndpoint(endpoint: String) = apply { settings.endpoint = endpoint }
    fun setReturnUrl(returnUrl: String) = apply { settings.returnUrl = returnUrl }
    fun setNotificationUrl(notificationUrl: String) = apply { settings.notificationUrl = notificationUrl }
    fun setTransactionType(transactionType: TransactionType) = apply { settings.transactionType = transactionType }
    fun setTestMode(isTestMode: Boolean) = apply { settings.isTestMode = isTestMode }
    fun setDebugMode(isDebugMode: Boolean) = apply { settings.isDebugMode = isDebugMode }
    fun setPublicKey(publicKey: String) = apply { settings.publicKey = publicKey }

    fun build() = PaymentSdk.instance.apply { applySettings(settings) }
}