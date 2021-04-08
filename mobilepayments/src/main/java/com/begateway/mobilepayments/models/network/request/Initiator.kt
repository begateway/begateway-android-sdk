package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class Initiator {
    @SerializedName("merchant") MERCHANT,
    @SerializedName("customer") CUSTOMER,
}