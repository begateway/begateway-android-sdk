package com.begateway.mobilepayments.model

import com.google.gson.annotations.SerializedName

class Brands (
    @SerializedName("alternative") val alternative : Boolean,
    @SerializedName("name") val name : String,
    @SerializedName("required_fields") val required_fields : String?
)