package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class ReadOnly {
    @SerializedName("email") EMAIL,
    @SerializedName("first_name") FIRST_NAME,
    @SerializedName("last_name") LAST_NAME,
    @SerializedName("address") ADDRESS,
    @SerializedName("city") CITY,
    @SerializedName("state") STATE,
    @SerializedName("zip") ZIP,
    @SerializedName("phone") PHONE,
    @SerializedName("country") COUNTRY,
}
