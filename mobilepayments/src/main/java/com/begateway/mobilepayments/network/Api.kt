package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.model.network.request.GetPaymentTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

internal const val CONTENT_TYPE = "Content-Type: application/json; charset=utf-8"
internal const val ACCEPT_HEADER = "Accept: application/json"
internal const val X_API_VERSION = "X-Api-Version: 2"
internal interface Api {
    @Headers(
            CONTENT_TYPE,
            ACCEPT_HEADER,
            X_API_VERSION,
    )
    @POST("checkouts")
    suspend fun getPaymentToken(
            @Header("Authorization") authorization: String,
            @Body requestBody: GetPaymentTokenRequest
    ): Response<Any>
}