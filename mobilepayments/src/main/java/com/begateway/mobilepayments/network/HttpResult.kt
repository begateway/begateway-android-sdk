package com.begateway.mobilepayments.network

import retrofit2.Response

sealed class HttpResult<out T> {
    data class Success<out T>(val data: T) : HttpResult<T>()
    data class UnSuccess<T>(val response: Response<T>) : HttpResult<T>()
    data class Error(val exception: Exception) : HttpResult<Nothing>()
}