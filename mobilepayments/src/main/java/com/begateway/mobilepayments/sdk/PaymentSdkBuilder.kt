package com.begateway.mobilepayments.sdk

import androidx.annotation.Keep
import com.begateway.mobilepayments.models.settings.PaymentSdkSettings

@Keep
class PaymentSdkBuilder {
    @Keep
    private val settings = PaymentSdkSettings()

    @Keep
    fun setEndpoint(endpoint: String) = apply { settings.endpoint = endpoint }

    @Keep
    fun setDebugMode(isDebug: Boolean) = apply { settings.isDebugMode = isDebug }

    @Keep
    fun setPublicKey(publicKey: String) = apply { settings.publicKey = publicKey }

    @Keep
    fun setCardNumberFieldVisibility(isVisible: Boolean) =
        apply { settings.isCardNumberFieldVisible = isVisible }

    @Keep
    fun setCardHolderFieldVisibility(isVisible: Boolean) =
        apply { settings.isCardHolderFieldVisible = isVisible }

    @Keep
    fun setCardDateFieldVisibility(isVisible: Boolean) =
        apply { settings.isCardDateFieldVisible = isVisible }

    @Keep
    fun setCardCVCFieldVisibility(isVisible: Boolean) =
        apply { settings.isCardCVCFieldVisible = isVisible }

    @Keep
    fun setNFCScanVisibility(isVisible: Boolean) =
        apply { settings.isNFCScanVisible = isVisible }

    @Keep
    @Throws(IllegalArgumentException::class)
    fun build(): PaymentSdk {
        require(settings.publicKey.isNotBlank()) { "Public key can not be empty" }
        require(settings.endpoint.isNotBlank()) { "Endpoint can not be empty" }
        return PaymentSdk.instance.apply {
            initSdk(
                this@PaymentSdkBuilder.settings,
            )
        }
    }
}