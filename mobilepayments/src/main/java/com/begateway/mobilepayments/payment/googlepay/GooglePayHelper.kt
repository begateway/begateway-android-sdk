package com.begateway.mobilepayments.payment.googlepay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import com.begateway.mobilepayments.models.googlepay.android.request.*
import com.begateway.mobilepayments.models.googlepay.android.request.TransactionInfo
import com.begateway.mobilepayments.models.googlepay.android.response.GooglePayResponse
import com.begateway.mobilepayments.models.network.request.Order
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.utils.getFormattedAmount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import java.util.*

internal object GooglePayHelper {
    private val CARD_TYPES = listOf("VISA", "MASTERCARD")
    private val CARD_AUTH = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    fun startPaymentFlow(
        activity: Activity,
        requestCode: Int,
        order: Order
    ) {
        AutoResolveHelper.resolveTask(
            createPaymentsClient(activity).loadPaymentData(
                getPaymentDataRequest(
                    order
                )
            ),
            activity,
            requestCode
        )
    }

    private fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(if (PaymentSdk.instance.sdkSettings.isDebugMode) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    @SuppressLint("DefaultLocale")
    private fun getPaymentDataRequest(order: Order): PaymentDataRequest {
        val currency = Currency.getInstance(order.currency)
        val googlePay = PaymentSdk.instance.checkoutWithTokenData!!.checkout.googlePay!!
        return PaymentDataRequestLocal(
            allowedPaymentMethods = arrayListOf(
                AllowedPaymentMethods(
                    parameters = PaymentTypeParameters(
                        CARD_AUTH,
                        CARD_TYPES
                    ),
                    tokenizationSpecification = TokenizationSpecification(
                        parameters = Parameters(
                            googlePay.gateway_id,
                            googlePay.gateway_merchant_id
                        )
                    )
                )
            ),
            transactionInfo = TransactionInfo(
                totalPrice = order.amount.getFormattedAmount(currency),
                currencyCode = currency.currencyCode
            )
        ).convertToPaymentRequest()
    }

    @JvmStatic
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

    fun getGooglePayResponse(data: Intent): GooglePayResponse? =
        GooglePayResponse.getGooglePayResponse(data)
}
