package com.begateway.mobilepayments

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.begateway.mobilepayments.model.CardData
import com.begateway.mobilepayments.model.PaymentSdkSettings
import com.begateway.mobilepayments.model.network.request.GetPaymentTokenRequest
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.ui.CheckoutActivity

class PaymentSdk private constructor() {
    private lateinit var settings: PaymentSdkSettings
    private lateinit var rest: Rest

    internal fun applySettings(settings: PaymentSdkSettings) {
        this.settings = settings
        rest = Rest(settings.endpoint, settings.isDebugMode)
    }

    suspend fun getPaymentToken(
        publicKey: String,
        requestBody: GetPaymentTokenRequest
    ): HttpResult<Any> {
        return rest.getPaymentToken(publicKey, requestBody)
    }

    companion object {
        @[JvmStatic Keep]
        fun getCardFormIntent(context: Context) =
            Intent(context, CheckoutActivity::class.java)

        @[JvmStatic JvmOverloads Keep]
        fun getCardDataIntent(
            cardNumber: String? = null,
            cardHolderName: String? = null,
            expiryMonth: String? = null,
            expiryYear: String? = null,
            cvcCode: String? = null
        ) = CardData.getIntent(
            cardNumber,
            cardHolderName,
            expiryMonth,
            expiryYear,
            cvcCode
        )

        @[JvmStatic JvmOverloads Keep]
        fun getCardDataIntentWithExpiryString(
            cardNumber: String? = null,
            cardHolderName: String? = null,
            expiryString: String? = null,
            cvcCode: String? = null
        ) = CardData.getIntentWithExpiryString(
            cardNumber,
            cardHolderName,
            expiryString,
            cvcCode
        )

        @JvmStatic
        internal var instance = PaymentSdk()
    }
}