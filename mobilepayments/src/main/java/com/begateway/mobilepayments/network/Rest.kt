package com.begateway.mobilepayments.network

import android.util.Log
import com.begateway.mobilepayments.model.network.request.PaymentRequest
import com.begateway.mobilepayments.model.network.request.TokenCheckoutData
import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.CheckoutWithTokenData
import com.begateway.mobilepayments.parser.BepaidResponseParser
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

internal class Rest(baseUrl: String, isDebugMode: Boolean) {

    private val retrofit: Retrofit
    private val api: Api

    init {
        val client = OkHttpClient.Builder().run {
            if (isDebugMode) addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            build()
        }
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(BepaidResponse::class.java, BepaidResponseParser())
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build()

        api = retrofit.create(Api::class.java)
    }

    suspend fun getPaymentToken(
        publicKey: String,
        requestBody: TokenCheckoutData
    ): HttpResult<CheckoutWithTokenData> {
        return safeApiCall { api.getPaymentToken(BEARER_AUTH_STRING + publicKey, requestBody) }
    }

    suspend fun payWithCard(
        publicKey: String,
        requestBody: PaymentRequest
    ): HttpResult<BepaidResponse> {
        return safeApiCall { api.payWithCard(BEARER_AUTH_STRING + publicKey, requestBody) }
    }

    suspend fun getPaymentStatus(
        publicKey: String,
        token: String
    ): HttpResult<BepaidResponse> {
        return safeApiCall { api.getPaymentStatus(BEARER_AUTH_STRING + publicKey, token) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): HttpResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                HttpResult.Success(response.body()!!)
            } else {
                HttpResult.UnSuccess(
                    BepaidResponseParser().parseJson(
                        Gson().fromJson(response.errorBody()?.string(), JsonElement::class.java)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("mobilepayments", "unknown error", e)
            HttpResult.Error(e)
        }
    }

}