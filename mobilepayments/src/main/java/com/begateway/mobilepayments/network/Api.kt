package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import retrofit2.Response
import retrofit2.http.*

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
        @Body requestBody: TokenCheckoutData
    ): Response<CheckoutWithTokenData>

    @Headers(
        CONTENT_TYPE,
        ACCEPT_HEADER,
        X_API_VERSION,
    )
    @POST("payments")
    suspend fun payWithCard(
        @Header("Authorization") authorization: String,
        @Body requestBody: PaymentRequest
    ): Response<BeGatewayResponse>

    @Headers(
        CONTENT_TYPE,
        ACCEPT_HEADER,
        X_API_VERSION,
    )
    @GET("checkouts/{token}")
    suspend fun getPaymentStatus(
        @Header("Authorization") authorization: String,
        @Path("token") token: String
    ): Response<BeGatewayResponse>
}