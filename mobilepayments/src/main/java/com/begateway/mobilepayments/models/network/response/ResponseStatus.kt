package com.begateway.mobilepayments.models.network.response

import com.google.gson.annotations.SerializedName

enum class ResponseStatus(val status: String) {
    @SerializedName("successful")
    SUCCESS("successful"),

    @SerializedName("error")
    ERROR("error"),

    @SerializedName("canceled")
    CANCELED("canceled"),

    @SerializedName("incomplete")
    INCOMPLETE("incomplete"),

    @SerializedName("failed")
    FAILED("failed"),

    @SerializedName("time_out")
    TIME_OUT("time_out");

    companion object {
        fun getStatus(string: String?): ResponseStatus =
            values().find {
                it.status.equals(string, true)
            } ?: ERROR
    }
}