package com.begateway.mobilepayments.payment.googlepay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import com.begateway.mobilepayments.models.googlepay.request.*
import com.begateway.mobilepayments.models.googlepay.request.TransactionInfo
import com.begateway.mobilepayments.models.googlepay.response.GooglePayResponse
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.utils.getFormattedAmount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import java.util.*

object GooglePayHelper {
    private val CARD_TYPES = listOf("VISA", "MASTERCARD")
    private val CARD_AUTH = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    private val GATEWAY = "ecomcharge"
    fun startPaymentFlow(
        activity: Activity
    ) {
        AutoResolveHelper.resolveTask(
            createPaymentsClient(activity).loadPaymentData(
                getPaymentDataRequest(
                    paymentMethod,
                    orderConfirmation
                )
            ),
            activity,
            GOOGLE_PAY_APP_REQUEST_CODE
        )
    }

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(if (PaymentSdk.instance.settings.isDebugMode) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    @SuppressLint("DefaultLocale")
    private fun getPaymentDataRequest(): PaymentDataRequest = PaymentDataRequestLocal(
        allowedPaymentMethods = arrayListOf(
            AllowedPaymentMethods(
                parameters = PaymentTypeParameters(
                    CARD_AUTH,
                    CARD_TYPES
                ),
                tokenizationSpecification = TokenizationSpecification(
                    parameters = Parameters(
                        GATEWAY,
                        PaymentSdk.instance.settings.publicKey
                    )
                )
            )
        ),
        transactionInfo = TransactionInfo(
            totalPrice = "100".getFormattedAmount(Currency.getInstance("USD")),
            currencyCode = "USD"
        )
    ).convertToPaymentRequest()

    fun checkIsReadyToPayTask(
        activity: Activity,
        onChecked: (isSuccess: Boolean) -> Unit
    ) {
        createPaymentsClient(activity).isReadyToPay(getIsReadyToPayRequest())
            .addOnCompleteListener { completedTask ->
                var isTaskSuccess = false
                try {
                    completedTask.getResult(ApiException::class.java)?.let { isSuccess ->
                        isTaskSuccess = isSuccess
                    }
                } catch (exception: ApiException) {
                    // Process error
                    Log.w("isReadyToPay failed", exception)
                }
                onChecked(isTaskSuccess)
            }
    }

    @SuppressLint("DefaultLocale")
    private fun getIsReadyToPayRequest(): IsReadyToPayRequest =
        IsReadyToPayRequestLocal(
            allowedPaymentMethods = arrayListOf(
                AllowedPaymentMethods(
                    parameters = PaymentTypeParameters(
                        CARD_AUTH,
                        CARD_TYPES
                    ),
                    tokenizationSpecification = null
                )
            )
        ).convertToIsReadyToPayRequest()

    fun getEncodedToken(token: String) = String(Base64.encode(token.toByteArray(), Base64.NO_WRAP))

    fun getGooglePayResponse(data: Intent): GooglePayResponse? =
        GooglePayResponse.getGooglePayResponse(data)
}
