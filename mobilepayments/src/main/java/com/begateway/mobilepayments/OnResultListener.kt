package com.begateway.mobilepayments

import androidx.annotation.Keep
import com.begateway.mobilepayments.model.network.response.BepaidResponse

@Keep
interface OnResultListener {
    @Keep
    fun onTokenReady(token: String)

    @Keep
    fun onPaymentFinished(bepaidResponse: BepaidResponse, cardToken: String?)
}