package com.begateway.mobilepayments.network

import android.util.Log
import com.begateway.mobilepayments.model.network.request.GetPaymentTokenRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal const val BEARER_AUTH_STRING = "Basic "

internal class Rest(baseUrl: String, isDebugMode: Boolean) {

    private val retrofit: Retrofit
    private val api: Api

    init {
        val client = OkHttpClient.Builder().run {
            if (isDebugMode) addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            build()
        }
        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        api = retrofit.create(Api::class.java)
    }

    internal suspend fun getPaymentToken(publicKey: String, requestBody: GetPaymentTokenRequest): HttpResult<Any> {
        return safeApiCall { api.getPaymentToken(BEARER_AUTH_STRING + publicKey, requestBody) }
    }

    private suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): HttpResult<T> {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                HttpResult.Success(response.body()!!)
            } else {
                HttpResult.UnSuccess(response)
            }
        } catch (e: Exception) {
            Log.e("mobilepayments", "unknown error", e)
            HttpResult.Error(e)
        }
    }
}