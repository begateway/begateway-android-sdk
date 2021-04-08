package com.begateway.mobilepayments.network

import android.util.Log
import com.begateway.mobilepayments.models.network.request.PaymentRequest
import com.begateway.mobilepayments.models.network.request.TokenCheckoutData
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.parser.BeGatewayResponseParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


internal const val BEARER_AUTH_STRING = "Bearer "
internal const val BASIC_AUTH_STRING = "Basic "

internal const val CONTENT_TYPE = "Content-Type"
internal const val CONTENT_TYPE_VALUE = "application/json; charset=utf-8"
internal const val ACCEPT_HEADER = "Accept"
internal const val ACCEPT_HEADER_VALUE = " application/json"
internal const val X_API_VERSION = "X-Api-Version"
internal const val X_API_VERSION_VALUE = "2"
internal const val AUTORIZATION_HEADER_NAME = "Authorization"

internal class Rest(baseUrl: String, isDebugMode: Boolean, publicKey: String) {

    private val api: Api
    private val gson: Gson = Gson()

    init {
        val client = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .addHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                    .addHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE)
                    .addHeader(X_API_VERSION, X_API_VERSION_VALUE)
                    .addHeader(AUTORIZATION_HEADER_NAME, BEARER_AUTH_STRING + publicKey)
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            if (isDebugMode) addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }.build()
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(BeGatewayResponse::class.java, BeGatewayResponseParser())

        api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build()
            .create(Api::class.java)
    }

    suspend fun getPaymentToken(
        requestBody: TokenCheckoutData
    ): HttpResult<CheckoutWithTokenData> {
        return safeApiCall { api.getPaymentToken(requestBody) }
    }

    suspend fun payWithCard(
        requestBody: PaymentRequest
    ): HttpResult<BeGatewayResponse> {
        return safeApiCall { api.payWithCard(requestBody) }
    }

    suspend fun getPaymentStatus(
        token: String
    ): HttpResult<BeGatewayResponse> {
        return safeApiCall { api.getPaymentStatus(token) }
    }

    private suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): HttpResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                HttpResult.Success(response.body()!!)
            } else {
                HttpResult.UnSuccess(
                    BeGatewayResponseParser().parseJson(
                        gson.fromJson(response.errorBody()?.charStream(), JsonElement::class.java)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("mobilepayments", "unknown error", e)
            HttpResult.Error(e)
        }
    }

}