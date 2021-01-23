package com.begateway.mobilepayments.model.network.response

enum class ResponseStatus(val status: String) {
    SUCCESS("successful"),
    ERROR("error"),
    CANCELED("canceled"),
    INCOMPLETE("incomplete"),
    FAILED("failed");

    companion object {
        fun getStatus(string: String?): ResponseStatus =
            values().find {
                it.status.equals(string, true)
            } ?: ERROR
    }
}