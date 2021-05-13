package com.begateway.mobilepayments.models.googlepay.android.response

import android.content.Intent
import com.google.android.gms.wallet.PaymentData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class GooglePayResponse(
        @SerializedName("apiVersion") val apiVersion: Int,
        @SerializedName("apiVersionMinor") val apiVersionMinor: Int,
        @SerializedName("paymentMethodData") val paymentMethodData: PaymentMethodData
) {
    companion object {
        fun getGooglePayResponse(data: Intent): GooglePayResponse? = PaymentData.getFromIntent(data)?.let {
            Gson().fromJson(it.toJson(), GooglePayResponse::class.java)
        }
    }
}