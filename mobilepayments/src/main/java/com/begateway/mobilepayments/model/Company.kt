package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

class Company (
    @SerializedName("name") val name : String,
    @SerializedName("site") val site : String
)