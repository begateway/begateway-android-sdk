package com.begateway.mobilepayments

import android.app.Activity
import com.begateway.mobilepayments.model.PaymentSdkSettings
import kotlinx.coroutines.CoroutineScope

class PaymentSdkBuilder {
    private val settings = PaymentSdkSettings()

    fun setEndpoint(endpoint: String) = apply { settings.endpoint = endpoint }
    fun setDebugMode(isDebug: Boolean) = apply { settings.isDebugMode = isDebug }
    fun setPublicKey(publicKey: String) = apply { settings.publicKey = publicKey }
    fun setReturnUrl(returnUrl: String) = apply { settings.returnUrl = returnUrl }

    @Throws(IllegalArgumentException::class)
    fun build(
        activity: Activity,
        callback: OnResultListener,
        coroutineScope: CoroutineScope
    ): PaymentSdk {
        require(settings.publicKey.isNotBlank()) { "Public key can not be empty" }
        require(settings.endpoint.isNotBlank()) { "Endpoint can not be empty" }
        require(settings.returnUrl.isNotBlank()) { "Return url can not be empty" }
        return PaymentSdk.instance.apply {
            initSdk(
                this@PaymentSdkBuilder.settings,
                activity
            )
            addCallBackListener(callback)
            this.coroutineScope = coroutineScope
        }
    }
}