package com.begateway.mobilepayments.models.network.response

import com.google.gson.annotations.SerializedName

data class SamsungPayResponse(
    @SerializedName("resultCode") val resultCode: Int?,
    @SerializedName("resultMessage") val resultMessage: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("href") val href: String?,
    @SerializedName("encInfo") val encInfo: EncInfo?
)

data class EncInfo(
    @SerializedName("mod") val mod: String?,
    @SerializedName("exp") val exp: Int?,
    @SerializedName("keyId") val keyIdId: String?
)