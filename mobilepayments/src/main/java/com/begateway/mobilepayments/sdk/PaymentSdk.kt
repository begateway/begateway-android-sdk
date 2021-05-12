package com.begateway.mobilepayments.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.begateway.mobilepayments.encryption.RSA
import com.begateway.mobilepayments.models.network.gateway.GatewayPaymentRequest
import com.begateway.mobilepayments.models.network.request.CreditCard
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.models.network.response.ResponseStatus
import com.begateway.mobilepayments.models.settings.PaymentSdkSettings
import com.begateway.mobilepayments.models.ui.CardData
import com.begateway.mobilepayments.network.HttpResult
import com.begateway.mobilepayments.network.Rest
import com.begateway.mobilepayments.payment.googlepay.GooglePayHelper
import com.begateway.mobilepayments.ui.CheckoutActivity
import com.begateway.mobilepayments.ui.WebViewActivity
import com.google.android.gms.wallet.AutoResolveHelper
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
    internal lateinit var settings: PaymentSdkSettings
    internal val isSdkInitialized: Boolean
        get() = checkoutWithTokenData != null

    private lateinit var rest: Rest
    private val callbacks: ArrayList<OnResultListener> = arrayListOf()
    private var token: String? = null
    private var gatewayPaymentRequest: GatewayPaymentRequest? = null

    @Keep
    var checkoutWithTokenData: CheckoutWithTokenData? = null

    internal fun initSdk(
        settings: PaymentSdkSettings,
    ) {
        resetValues()
        this.settings = settings
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
        when (val pay = rest.payWithCard(requestBody)) {
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

    @Keep
    fun encryptData(data: String) = RSA.encryptData(data, settings.publicKey)

    internal suspend fun onThreeDSecureComplete() {
        token?.let {
            checkPaymentStatus(it)
        }
    }

    @Keep
    suspend fun checkPaymentStatus(token: String) {
        getPaymentStatus(token)
    }

    @Keep
    fun checkGoogleButtonVisibility(
        activity: Activity,
        onChecked: (isSuccess: Boolean) -> Unit
    ) = GooglePayHelper.checkIsReadyToPayTask(activity, onChecked)

    @Keep
    fun initGooglePay(
        activity: Activity,
        returnCode: Int,
        gatewayPaymentRequest: GatewayPaymentRequest
    ) {
        this.gatewayPaymentRequest = gatewayPaymentRequest
        GooglePayHelper.startPaymentFlow(activity, returnCode, gatewayPaymentRequest.request)
    }

    @Keep
    suspend fun payWithGooglePay(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            GooglePayHelper.getGooglePayResponse(data)?.let { gPayResponse ->
                when (
                    val pay = rest.payWithCardGateway(
                        getRequestWithGooglePayToken(
                            gPayResponse.paymentMethodData.tokenizationData.token
                        )
                    )
                ) {
                    is HttpResult.Success -> {
                        withContext(Dispatchers.Main) {
                            onPaymentFinished(pay.data)
                        }
                    }
                    else -> onNotSuccess(pay)
                }
                return
            }
        } else {
            if (resultCode == AutoResolveHelper.RESULT_ERROR) {
                AutoResolveHelper.getStatusFromIntent(data)?.let {
                    withContext(Dispatchers.Main) {
                        onPaymentFinished(
                            BeGatewayResponse(
                                message = "statusCode = ${it.statusCode}, message = ${it.statusMessage}"
                            )
                        )
                    }
                }
                return
            }
        }
        withContext(Dispatchers.Main) {
            onPaymentFinished(
                BeGatewayResponse()
            )
        }
    }

    private fun getRequestWithGooglePayToken(token: String): GatewayPaymentRequest {
        val formattedToken = GooglePayHelper.TOKEN_PREFIX + GooglePayHelper.getEncodedToken(
            token
        )
        return gatewayPaymentRequest!!.copy(
            request = gatewayPaymentRequest!!.request.copy(
                creditCard = gatewayPaymentRequest!!.request.creditCard?.copy(
                    token = formattedToken
                ) ?: CreditCard(token = formattedToken)
            )
        )
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
                is HttpResult.Success -> TODO()
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
        gatewayPaymentRequest = null
    }
}