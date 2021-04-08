package com.begateway.mobilepayments.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.begateway.mobilepayments.encryption.RSA
import com.begateway.mobilepayments.models.network.request.CreditCard
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.Request
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.models.network.response.ResponseStatus
import com.begateway.mobilepayments.models.settings.PaymentSdkSettings
import com.begateway.mobilepayments.models.ui.CardData
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.ui.CheckoutActivity
import com.begateway.mobilepayments.ui.WebViewActivity
import kotlinx.coroutines.*

@Keep
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

        private const val TIMEOUT_MILLISECONDS: Long = 60000L
        private const val ATTEMPT_INTERVAL_MILLISECONDS: Long = 5000L
        internal val instance = PaymentSdk()
    }

    internal var isSaveCard: Boolean = false
    internal var cardToken: String? = null
    internal lateinit var settings: PaymentSdkSettings
    internal val isSdkInitialized: Boolean
        get() = checkoutWithTokenData != null

    private lateinit var rest: Rest
    private val callbacks: ArrayList<OnResultListener> = arrayListOf()
    private var token: String? = null

    @Keep
    var checkoutWithTokenData: CheckoutWithTokenData? = null

    //only for test remove in prod
    fun updatePublicKey(key: String) {
        settings.publicKey = key
    }

    internal fun initSdk(
        settings: PaymentSdkSettings,
    ) {
        this.settings = settings
        rest = Rest(settings.endpoint, settings.isDebugMode, settings.publicKey)
    }

    @Keep
    fun addCallBackListener(onResultListener: OnResultListener) {
        callbacks.add(onResultListener)
    }

    @Keep
    fun removeResultListener(onResultListener: OnResultListener) {
        callbacks.remove(onResultListener)
    }

    @Keep
    suspend fun getPaymentToken(
        requestBody: TokenCheckoutData
    ) {
        settings.returnUrl = requestBody.checkout.settings.returnUrl
        when (val paymentToken = rest.getPaymentToken(requestBody)) {
            is HttpResult.Success -> {
                val data = paymentToken.data
                checkoutWithTokenData = data
                withContext(Dispatchers.Main) {
                    callbacks.forEach {
                        it.onTokenReady(data.checkout.token)
                    }
                }
            }
            else -> onNotSuccess(paymentToken)
        }
    }


    @Keep
    suspend fun payWithCard(
        requestBody: PaymentRequest,
        activity: Activity
    ) {
        val localRequestBody = applyEncryption(requestBody)
        when (val pay = rest.payWithCard(localRequestBody)) {
            is HttpResult.Success -> {
                val data = pay.data
                if (data.status == ResponseStatus.INCOMPLETE && data.threeDSUrl != null) {
                    token = data.paymentToken
                    withContext(Dispatchers.Main) {
                        activity.startActivity(
                            WebViewActivity.getThreeDSIntent(
                                activity,
                                data.threeDSUrl
                            )
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onPaymentFinished(data)
                    }
                }
            }
            else -> onNotSuccess(pay)
        }
    }

    private fun applyEncryption(requestBody: PaymentRequest) =
        if (settings.isWithEncryption) {
            val request = requestBody.request
            val creditCard = request.creditCard
            val cardNumber = creditCard?.cardNumber
            val cvc = creditCard?.cvc
            val holderName = creditCard?.holderName
            val expMonth = creditCard?.expMonth
            val expYear = creditCard?.expYear
            requestBody.copy(
                request = Request(
                    token = request.token,
                    paymentMethod = request.paymentMethod,
                    creditCard = null,
                    encryptedCreditCard = CreditCard(
                        cardNumber = if (cardNumber != null) {
                            RSA.encryptData(
                                cardNumber,
                                settings.publicKey
                            )
                        } else {
                            null
                        },
                        cvc = if (cvc != null) {
                            RSA.encryptData(
                                cvc,
                                settings.publicKey
                            )
                        } else {
                            null
                        },
                        holderName = if (holderName != null) {
                            RSA.encryptData(
                                holderName,
                                settings.publicKey
                            )
                        } else {
                            null
                        },
                        expMonth = if (expMonth != null) {
                            RSA.encryptData(
                                expMonth,
                                settings.publicKey
                            )
                        } else {
                            null
                        },
                        expYear = if (expYear != null) {
                            RSA.encryptData(
                                expYear,
                                settings.publicKey
                            )
                        } else {
                            null
                        },
                        token = creditCard?.token
                    )
                )
            )
        } else {
            requestBody
        }

    internal suspend fun onThreeDSecureComplete() {
        token?.let {
            checkPaymentStatus(it)
        }
    }

    @Keep
    suspend fun checkPaymentStatus(token: String) {
        getPaymentStatus(token)
    }

    private suspend fun getPaymentStatus(token: String) {
        var startTime = 0L
        var beGatewayResponse = BeGatewayResponse(
            status = ResponseStatus.TIME_OUT,
            message = "Please check payment status"
        )
        while (startTime < TIMEOUT_MILLISECONDS) {
            val currentTime = System.currentTimeMillis()
            when (val paymentStatus = rest.getPaymentStatus(token)) {
                is HttpResult.Success -> {
                    beGatewayResponse = paymentStatus.data
                    if (beGatewayResponse.status != ResponseStatus.INCOMPLETE) {
                        withContext(Dispatchers.Main) {
                            onPaymentFinished(paymentStatus.data)
                        }
                        return
                    }
                }
                else -> {
                    onNotSuccess(paymentStatus)
                    return
                }
            }
            delay(ATTEMPT_INTERVAL_MILLISECONDS)
            startTime += (System.currentTimeMillis() - currentTime)
        }
        withContext(Dispatchers.Main) {
            onPaymentFinished(beGatewayResponse)
        }
    }

    private suspend fun <T : Any> onNotSuccess(result: HttpResult<T>) {
        withContext(Dispatchers.Main) {
            when (result) {
                is HttpResult.UnSuccess -> onPaymentFinished(result.beGatewayResponse)
                is HttpResult.Error -> onPaymentFinished(result.beGatewayResponse)
            }
        }
    }

    internal fun onPaymentFinished(beGatewayResponse: BeGatewayResponse) {
        callbacks.forEach {
            it.onPaymentFinished(
                beGatewayResponse = beGatewayResponse,
                cardToken = cardToken
            )
        }
        resetValues()
    }

    internal fun resetValues() {
        isSaveCard = false
        cardToken = null
        token = null
        checkoutWithTokenData = null
    }
}