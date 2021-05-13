package com.begateway.mobilepayments.models.network

import com.begateway.mobilepayments.models.network.request.CreditCard
import com.google.gson.annotations.SerializedName

class CardInfo(
    @SerializedName("credit_cards") val credit_cards: List<CreditCard>?,
    @SerializedName("uuid") val uuid: String?
) : AdditionalFields()