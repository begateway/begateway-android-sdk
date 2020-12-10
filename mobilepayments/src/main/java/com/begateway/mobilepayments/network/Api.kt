package com.begateway.mobilepayments.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {
    @Headers(
            "Content-Type: application/json; utf-8",
            "Accept: application/json",
            "X-Api-Version: 2",
    )
    @POST("path")
    suspend fun getPaymentToken(
            @Header("Authorization") authorization: String,
            @Body body: Any
    ): Response<Any>
}