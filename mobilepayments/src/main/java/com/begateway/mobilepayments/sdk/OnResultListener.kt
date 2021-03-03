package com.begateway.mobilepayments.sdk

import androidx.annotation.Keep
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse

@Keep
interface OnResultListener {
    @Keep
    fun onTokenReady(token: String)

    @Keep
    fun onPaymentFinished(beGatewayResponse: BeGatewayResponse, cardToken: String?)
}