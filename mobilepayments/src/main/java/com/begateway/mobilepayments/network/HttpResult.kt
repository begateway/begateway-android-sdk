package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.ResponseStatus

internal sealed class HttpResult<out T> {
    data class Success<out T>(val data: T) : HttpResult<T>()
    data class UnSuccess<T>(val bepaidResponse: BepaidResponse) : HttpResult<T>()
    data class Error(private val exception: Exception) : HttpResult<Nothing>() {
        val bepaidResponse: BepaidResponse
            get() = BepaidResponse(
                ResponseStatus.ERROR,
                exception.message
            )
    }
}