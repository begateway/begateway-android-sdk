package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

class CheckoutWithToken(
    @SerializedName("token") val token: String,
    @SerializedName("redirect_url") val redirect_url: String? = null,
    @SerializedName("brands") val brands: List<Brands>? = null,
    @SerializedName("company") val company: Company? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("card_info") val card_info: CardInfo? = null
) : AdditionalFields()