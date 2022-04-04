package com.begateway.mobilepayments.sdk

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import com.begateway.mobilepayments.encryption.RSA
import com.begateway.mobilepayments.models.googlepay.android.response.GooglePayResponse
import com.begateway.mobilepayments.models.googlepay.api.GPaymentRequest
import com.begateway.mobilepayments.models.googlepay.api.GRequest
import com.begateway.mobilepayments.models.network.request.Order
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.models.network.response.PaymentData
import com.begateway.mobilepayments.models.network.response.ResponseStatus
import com.begateway.mobilepayments.models.settings.PaymentSdkSettings
import com.begateway.mobilepayments.models.ui.CardData
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.payment.googlepay.GooglePayHelper
import com.begateway.mobilepayments.ui.CheckoutActivity
import com.begateway.mobilepayments.ui.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Keep
class PaymentSdk private constructor() {
    companion object {
        @[JvmStatic Keep]
        fun getCardFormIntent(context: Context) =
            Intent(context, CheckoutActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

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
    internal lateinit var sdkSettings: PaymentSdkSettings
    internal val isSdkInitialized: Boolean
        get() = checkoutWithTokenData != null
    internal var paymentData: PaymentData? = null

    private lateinit var rest: Rest
    private val callbacks: ArrayList<OnResultListener> = arrayListOf()

    @Keep
    var checkoutWithTokenData: CheckoutWithTokenData? = null

    internal fun initSdk(
        settings: PaymentSdkSettings,
    ) {
        resetValues()
        this.sdkSettings = settings
        rest = Rest(settings.endpoint, settings.isDebugMode, settings.publicKey)
    }

    @Keep
    fun addCallBackListener(onResultListener: OnResultListener) {
        if (!callbacks.contains(onResultListener))
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
        when (val paymentToken = rest.getPaymentToken(requestBody)) {
            is HttpResult.Success -> {
                val data = paymentToken.data
                checkoutWithTokenData = data
                getPaymentData(
                    token = data.checkout.token,
                    onSuccess = {},
                    onError = {}
                )
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
        context: Context,
        launcher: ActivityResultLauncher<Intent>? = null
    ) {
        when (val pay = rest.payWithCard(requestBody)) {
            is HttpResult.Success -> {
                val data = pay.data
                if (data.status == ResponseStatus.INCOMPLETE && data.threeDSUrl != null && data.resultUrl != null) {
                    getPaymentData(
                        token = requestBody.request.token,
                        onSuccess = {
                            withContext(Dispatchers.Main) {
                                val threeDSIntent = WebViewActivity.getThreeDSIntent(
                                    context = context,
                                    url = data.threeDSUrl,
                                    resultUrl = data.resultUrl
                                )
                                (launcher?.let {
                                    launcher.launch(threeDSIntent)
                                } ?: kotlin.run {
                                    context.startActivity(threeDSIntent)
                                })
                            }
                        },
                        onError = {
                            onNotSuccess(it)
                        }
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        onPaymentFinished(data)
                    }
                }
            }
            else -> onNotSuccess(pay)
        }
    }

    private suspend fun getPaymentData(
        token: String,
        onSuccess: suspend (paymentData: PaymentData) -> Unit,
        onError: suspend (error: HttpResult<PaymentData>) -> Unit
    ) {
        paymentData
            ?.takeIf { it.checkout.token == token }
            ?.let { onSuccess(it) }
            ?: kotlin.run {
                when (val data = rest.getPaymentData(token)) {
                    is HttpResult.Success -> {
                        onSuccess(data.data.also { this.paymentData = it })
                    }
                    else -> onError(data)
                }
            }
    }

    @Keep
    fun encryptData(data: String) = RSA.encryptData(data, sdkSettings.publicKey)

    internal suspend fun onThreeDSecureComplete() {
        paymentData?.checkout?.token?.let {
            checkPaymentStatus(it)
        }
    }

    @Keep
    suspend fun checkPaymentStatus(token: String) {
        var startTime = 0L
        var beGatewayResponse = BeGatewayResponse(
            status = ResponseStatus.TIME_OUT,
            message = "Please check payment status"
        )
        while (startTime < TIMEOUT_MILLISECONDS) {
            val currentTime = System.currentTimeMillis()
            when (val paymentStatus = rest.getPaymentData(token)) {
                is HttpResult.Success -> {
                    val data = paymentStatus.data
                    val checkout = data.checkout
                    val status = checkout.status
                    beGatewayResponse = BeGatewayResponse(
                        status = status,
                        message = checkout.message,
                    )
                    if (status != ResponseStatus.INCOMPLETE) {
                        withContext(Dispatchers.Main) {
                            onPaymentFinished(beGatewayResponse)
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

    @Keep
    internal suspend fun payWithGooglePay(data: Intent) {
        GooglePayHelper.getGooglePayResponse(data)?.let { gPayResponse ->
            when (
                val pay = rest.payWithGooglePay(
                    getRequestWithGooglePayToken(
                        gPayResponse
                    )
                )
            ) {
                is HttpResult.Success -> {
                    onPaymentFinished(pay.data)
                }
                else -> onNotSuccess(pay)
            }
        } ?: onPaymentFinished(BeGatewayResponse())
    }

    private fun getRequestWithGooglePayToken(response: GooglePayResponse): GPaymentRequest {
        return GPaymentRequest(
            checkoutWithTokenData!!.checkout.token,
            GRequest(
                response.apiVersion,
                response.apiVersionMinor,
                response.paymentMethodData
            )
        )
    }

    internal suspend fun getOrderDetails(): Order? {
        val token = checkoutWithTokenData!!.checkout.token
        return paymentData
            ?.takeIf { it.checkout.token == token }
            ?.checkout
            ?.order
            ?: kotlin.run {
                when (val pay = rest.getPaymentData(token)) {
                    is HttpResult.Success -> {
                        pay.data.checkout.order
                    }
                    else -> null
                }
            }
    }


    private suspend fun <T : Any> onNotSuccess(result: HttpResult<T>) {
        withContext(Dispatchers.Main) {
            when (result) {
                is HttpResult.UnSuccess -> onPaymentFinished(result.beGatewayResponse)
                is HttpResult.Error -> onPaymentFinished(result.beGatewayResponse)
                is HttpResult.Success -> throw IllegalStateException("Can't work with success there")
            }
        }
    }

    internal fun onPaymentFinished(beGatewayResponse: BeGatewayResponse) {
        callbacks.forEach {
            it.onPaymentFinished(
                beGatewayResponse = beGatewayResponse,
                cardToken = if (beGatewayResponse.status == ResponseStatus.SUCCESS) {
                    cardToken
                } else {
                    null
                }
            )
        }
        resetValues()
    }

    internal fun resetValues() {
        isSaveCard = false
        cardToken = null
        checkoutWithTokenData = null
        paymentData = null
    }
}