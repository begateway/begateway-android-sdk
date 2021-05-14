package com.begateway.mobilepayments.models.googlepay.android.request

import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class IsReadyToPayRequestLocal(
    @SerializedName("apiVersion") val apiVersion: Int = 2,
    @SerializedName("apiVersionMinor") val apiVersionMinor: Int = 0,
    @SerializedName("allowedPaymentMethods") val allowedPaymentMethods: List<AllowedPaymentMethods>
) {
    fun convertToIsReadyToPayRequest(): IsReadyToPayRequest =
        IsReadyToPayRequest.fromJson(Gson().toJson(this))
}
