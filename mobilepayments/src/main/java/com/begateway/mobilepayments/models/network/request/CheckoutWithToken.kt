package com.begateway.mobilepayments.models.network.request

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.annotations.SerializedName

class CheckoutWithToken(
    @SerializedName("token") val token: String,
    @SerializedName("redirect_url") val redirect_url: String? = null,
    @SerializedName("company") val company: Company? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("card_info") val card_info: CardInfo? = null
) : AdditionalFields()