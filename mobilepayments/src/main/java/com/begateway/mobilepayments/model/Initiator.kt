package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

enum class Initiator {
    @SerializedName("merchant") MERCHANT,
    @SerializedName("customer") CUSTOMER,
}