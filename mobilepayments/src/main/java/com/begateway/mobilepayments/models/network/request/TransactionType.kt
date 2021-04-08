package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class TransactionType{
    @SerializedName("payment") PAYMENT,
    @SerializedName("authorization") AUTHORIZATION,
    @SerializedName("tokenization") TOKENIZATION
}
