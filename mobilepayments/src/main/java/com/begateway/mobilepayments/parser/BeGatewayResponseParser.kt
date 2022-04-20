package com.begateway.mobilepayments.parser

import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.ResponseStatus
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type


class BeGatewayResponseParser : JsonDeserializer<BeGatewayResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BeGatewayResponse = parseJson(json)

    fun parseJson(json: JsonElement?): BeGatewayResponse {
        return if (json != null && !json.isJsonNull) {
            var status: String? = null
            var message: String? = null
            var url: String? = null
            var resultUrl: String? = null
            var paymentToken: String? = null
            json.asJsonObject?.let { body ->
                val jsonObject = when {
                    body.has("response") -> {
                        val response = body.getAsJsonObject("response").asJsonObject
                        if (PaymentSdk.instance.isSaveCard) {
                            val creditCard = response.getAsJsonObject("credit_card")
                            if (creditCard != null && !creditCard.isJsonNull) {
                                PaymentSdk.instance.cardToken = getString("token", creditCard)
                            }
                        }
                        response
                    }
                    body.has("checkout") -> {
                        body.getAsJsonObject("checkout").asJsonObject
                    }
                    else -> {
                        body
                    }
                }
                status = getString("status", jsonObject)
                message = getString("message", jsonObject)
                url = getString("url", jsonObject)
                resultUrl = getString("result_url", jsonObject)
                paymentToken = getString("token", jsonObject)
            }

            BeGatewayResponse(
                status = ResponseStatus.getStatus(status),
                message = message,
                paymentToken = paymentToken,
                threeDSUrl = url,
                resultUrl = resultUrl
            )
        } else {
            BeGatewayResponse()
        }
    }

    private fun getString(key: String, json: JsonObject): String? {
        val value = json.get(key)
        return if (value == null || value.isJsonNull) {
            null
        } else {
            value.asString
        }
    }
}