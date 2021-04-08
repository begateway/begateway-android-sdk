package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

class CardInfo(
    @SerializedName("credit_cards") val credit_cards: List<CreditCard>?,
    @SerializedName("uuid") val uuid: String?
)