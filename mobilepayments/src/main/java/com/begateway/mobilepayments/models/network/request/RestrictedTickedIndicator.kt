package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class RestrictedTickedIndicator {
    @SerializedName("0") CAN_RETURN,
    @SerializedName("1") CANNOT_RETURN,
}