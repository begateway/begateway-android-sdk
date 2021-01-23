package com.begateway.mobilepayments

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.begateway.mobilepayments.model.CardData
import com.begateway.mobilepayments.model.PaymentSdkSettings
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.model.network.request.TokenCheckoutData
import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.ui.CheckoutActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentSdk private constructor() {
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

        internal val instance = PaymentSdk()
    }

    internal lateinit var settings: PaymentSdkSettings
    private lateinit var rest: Rest
    internal var isSaveCard: Boolean = false
    internal val isSdkInitialized: Boolean
        get() = ::checkoutWithTokenData.isInitialized
    lateinit var checkoutWithTokenData: CheckoutWithTokenData

    internal fun applySettings(settings: PaymentSdkSettings) {
        this.settings = settings
        rest = Rest(settings.endpoint, settings.isDebugMode)
    }

    @Keep
    suspend fun getPaymentToken(
        requestBody: TokenCheckoutData
    ): HttpResult<CheckoutWithTokenData> {
        val paymentToken = rest.getPaymentToken(settings.publicKey, requestBody)
        when (paymentToken) {
            is HttpResult.Success -> checkoutWithTokenData = paymentToken.data
        }
        return paymentToken
    }

    @Keep
    suspend fun payWithCard(
        requestBody: PaymentRequest
    ): HttpResult<BepaidResponse> {
        return rest.payWithCard(settings.publicKey, requestBody)
    }

}