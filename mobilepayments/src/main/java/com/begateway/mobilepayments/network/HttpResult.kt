package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.ResponseStatus

internal sealed class HttpResult<out T> {
    data class Success<out T>(val data: T) : HttpResult<T>()
    data class UnSuccess<T>(val beGatewayResponse: BeGatewayResponse) : HttpResult<T>()
    data class Error(private val exception: Exception) : HttpResult<Nothing>() {
        val beGatewayResponse: BeGatewayResponse
            get() = BeGatewayResponse(
                ResponseStatus.ERROR,
                exception.message
            )
    }
}