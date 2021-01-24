package com.begateway.mobilepayments

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.begateway.mobilepayments.model.CardData
import com.begateway.mobilepayments.model.PaymentSdkSettings
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.model.network.request.TokenCheckoutData
import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.model.network.response.ResponseStatus
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.ui.CheckoutActivity
import com.begateway.mobilepayments.ui.WebViewActivity
import kotlinx.coroutines.*

private const val THREE_D_SECURE_CANCELLED = "three D secure was cancelled"

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

    internal lateinit var settings: PaymentSdkSettings
    private lateinit var rest: Rest

    internal val isSdkInitialized: Boolean
        get() = ::checkoutWithTokenData.isInitialized
    internal var activity: Activity? = null
    internal var callbacks: ArrayList<OnResultListener> = arrayListOf()
    internal var coroutineScope: CoroutineScope? = null

    internal var isSaveCard: Boolean = false
    private var token: String? = null
    internal var cardToken: String? = null

    @Keep
    lateinit var checkoutWithTokenData: CheckoutWithTokenData

    internal fun initSdk(
        settings: PaymentSdkSettings,
        activity: Activity
    ) {
        this.settings = settings
        this.activity = activity
        rest = Rest(settings.endpoint, settings.isDebugMode)
    }

    internal fun addCallBackListener(onResultListener: OnResultListener) {
        this.callbacks.add(onResultListener)
    }

    /**
     * don't forget to call removeResultListener(), this function will remove link to callback
     */
    @Keep
    fun removeResultListener(onResultListener: OnResultListener) {
        callbacks.remove(onResultListener)
    }

    /**
     * don't forget to call resetActivity(), this function will prevent memory leaking
     */
    @Keep
    fun resetActivity() {
        activity = null
    }

    @Keep
    fun resetCoroutineScope() {
        coroutineScope = null
    }

    @Keep
    fun getPaymentToken(
        requestBody: TokenCheckoutData
    ): Job? = coroutineScope?.launch {
        settings.returnUrl = requestBody.checkout.settings.returnUrl
        when (val paymentToken = rest.getPaymentToken(settings.publicKey, requestBody)) {
            is HttpResult.Success -> {
                checkoutWithTokenData = paymentToken.data
                withContext(Dispatchers.Main) {
                    callbacks.forEach {
                        it.onTokenReady(checkoutWithTokenData.checkout.token)
                    }
                }
            }
            else -> withContext(Dispatchers.Main) { onNotSuccess(paymentToken) }
        }
    }


    @Keep
    fun payWithCard(
        requestBody: PaymentRequest
    ): Job? =
        coroutineScope?.launch {
            when (val pay = rest.payWithCard(settings.publicKey, requestBody)) {
                is HttpResult.Success -> {
                    val data = pay.data
                    if (data.status == ResponseStatus.INCOMPLETE && data.threeDSUrl != null) {
                        token = data.paymentToken
                        withContext(Dispatchers.Main) {
                            activity?.startActivity(
                                WebViewActivity.getThreeDSIntent(
                                    activity!!,
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
                else -> withContext(Dispatchers.Main) { onNotSuccess(pay) }
            }
        }

    fun onThreeDSecureFinished(isCorrect: Boolean) {
        if (isCorrect) {
            token?.let {
                checkPaymentStatus(it)
            }
        } else {
            onPaymentFinished(
                BepaidResponse(
                    status = ResponseStatus.CANCELED,
                    message = THREE_D_SECURE_CANCELLED
                )
            )
        }
    }

    @Keep
    fun checkPaymentStatus(token: String): Job? = coroutineScope?.launch {
        getPaymentStatus(token)
    }

    private suspend fun getPaymentStatus(token: String) {
        var startTime = 0L
        var bepaidResponse = BepaidResponse(
            status = ResponseStatus.TIME_OUT,
            message = "Please check payment status"
        )
        while (startTime < TIMEOUT_MILLISECONDS) {
            val currentTime = System.currentTimeMillis()
            when (val paymentStatus = rest.getPaymentStatus(settings.publicKey, token)) {
                is HttpResult.Success -> {
                    bepaidResponse = paymentStatus.data
                    if (bepaidResponse.status != ResponseStatus.INCOMPLETE) {
                        withContext(Dispatchers.Main) {
                            onPaymentFinished(paymentStatus.data)
                        }
                        return
                    }
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        this@PaymentSdk.token = null
                        onNotSuccess(paymentStatus)
                    }
                    return
                }
            }
            delay(ATTEMPT_INTERVAL_MILLISECONDS)
            startTime += (System.currentTimeMillis() - currentTime)
        }
        withContext(Dispatchers.Main) {
            onPaymentFinished(bepaidResponse)
        }
    }

    private fun <T : Any> onNotSuccess(result: HttpResult<T>) {
        when (result) {
            is HttpResult.UnSuccess -> onPaymentFinished(result.bepaidResponse)
            is HttpResult.Error -> {
                onPaymentFinished(result.bepaidResponse)
            }
        }
    }

    private fun onPaymentFinished(bepaidResponse: BepaidResponse) {
        callbacks.forEach {
            it.onPaymentFinished(
                bepaidResponse = bepaidResponse,
                cardToken = cardToken
            )
        }
        isSaveCard = false
        cardToken = null
        token = null
    }
}