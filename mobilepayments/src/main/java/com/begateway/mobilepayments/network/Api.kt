package com.begateway.mobilepayments.network

import com.begateway.mobilepayments.models.googlepay.api.GPaymentRequest
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.SamsungPayTokenRequest
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.models.network.response.PaymentData
import com.begateway.mobilepayments.models.network.response.SamsungPayResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


internal interface Api {
    @POST("checkouts")
    suspend fun getPaymentToken(
        @Body requestBody: TokenCheckoutData
    ): Response<CheckoutWithTokenData>

    @POST("payments")
    suspend fun payWithCard(
        @Body requestBody: PaymentRequest
    ): Response<BeGatewayResponse>

    @GET("checkouts/{token}")
    suspend fun getPaymentData(
        @Path("token") token: String
    ): Response<PaymentData>

    @POST("google_pay/payment")
    suspend fun payWithGooglePay(
        @Body requestBody: GPaymentRequest
    ): Response<BeGatewayResponse>

    @POST("samsung_pay/create")
    suspend fun createSamsungPayTransaction(
        @Body requestBody: SamsungPayTokenRequest
    ): Response<SamsungPayResponse>

    @POST("samsung_pay/payment")
    suspend fun payWithSamsungPay(
        @Body requestBody: SamsungPayTokenRequest
    ): Response<BeGatewayResponse>
}