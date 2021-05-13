package com.begateway.mobilepayments.models.network

import com.google.gson.annotations.SerializedName

class CheckoutWithToken(
    @SerializedName("token") val token: String,
    @SerializedName("redirect_url") val redirectUrl: String? = null,
    @SerializedName("company") val company: Company? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("card_info") val cardInfo: CardInfo? = null,
    @SerializedName("google_pay") val googlePay: GooglePay? = null,
) : AdditionalFields()