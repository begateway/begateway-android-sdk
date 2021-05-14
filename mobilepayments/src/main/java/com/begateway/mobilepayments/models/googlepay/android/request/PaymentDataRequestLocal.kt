package com.begateway.mobilepayments.models.googlepay.android.request

import com.google.android.gms.wallet.PaymentDataRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class PaymentDataRequestLocal(
    @SerializedName("apiVersion") val apiVersion: Int = 2,
    @SerializedName("apiVersionMinor") val apiVersionMinor: Int = 0,
    @SerializedName("merchantInfo") val merchantInfo: MerchantInfo? = null,
    @SerializedName("allowedPaymentMethods") val allowedPaymentMethods: List<AllowedPaymentMethods>,
    @SerializedName("transactionInfo") val transactionInfo: TransactionInfo
) {
    fun convertToPaymentRequest(): PaymentDataRequest = PaymentDataRequest.fromJson(Gson().toJson(this))
}