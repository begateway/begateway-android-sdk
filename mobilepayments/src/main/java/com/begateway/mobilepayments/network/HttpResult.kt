package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.model.network.response.BepaidResponse

sealed class HttpResult<out T> {
    data class Success<out T>(val data: T) : HttpResult<T>()
    data class UnSuccess<T>(val bepaidResponse: BepaidResponse) : HttpResult<T>()
    data class Error(val exception: Exception) : HttpResult<Nothing>()
}