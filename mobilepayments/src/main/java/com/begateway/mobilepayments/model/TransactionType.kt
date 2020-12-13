package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

enum class TransactionType{
    @SerializedName("payment") PAYMENT,
    @SerializedName("authorization") AUTHORIZATION,
    @SerializedName("tokenization") TOKENIZATION
}
