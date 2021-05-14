package com.begateway.mobilepayments.models.googlepay.android.request

import com.google.gson.annotations.SerializedName

data class TransactionInfo(
    @SerializedName("totalPriceStatus") val totalPriceStatus: TotalPriceStatus = TotalPriceStatus.FINAL,
    @SerializedName("totalPrice") val totalPrice: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("countryCode") val countryCode: String? = null//ISO 3166-1 alpha-2 country code where the transaction is processed. This is required for merchants based in European Economic Area (EEA) countries.
)

enum class TotalPriceStatus {
    @SerializedName("FINAL")
    FINAL,

    @SerializedName("ESTIMATED")
    ESTIMATED,

    @SerializedName("NOT_CURRENTLY_KNOWN")
    NOT_CURRENTLY_KNOWN;
}