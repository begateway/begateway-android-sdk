package com.begateway.mobilepayments.parser

import com.begateway.mobilepayments.PaymentSdk
import com.begateway.mobilepayments.model.network.response.BepaidResponse
import com.begateway.mobilepayments.model.network.response.ResponseStatus
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type


class BepaidResponseParser : JsonDeserializer<BepaidResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BepaidResponse = parseJson(json)

    fun parseJson(json: JsonElement?): BepaidResponse {
        return if (json != null && !json.isJsonNull) {
            var status: String? = null
            var message: String? = null
            var cardToken: String? = null
            var url: String? = null
            json.asJsonObject?.let { body ->
                if (body.has("response")) {
                    val response = body.getAsJsonObject("response").asJsonObject
                    status = response?.get("status")?.asString
                    message = response?.get("message")?.asString
                    url = response?.get("url")?.asString
                    if (PaymentSdk.instance.isSaveCard) {
                        val creditCard = response.getAsJsonObject("credit_card")
                        if (creditCard != null && !creditCard.isJsonNull) {
                            cardToken = creditCard.get("token")?.asString
                        }
                    }
                } else {
                    status = body.get("status")?.asString
                    message = body.get("message")?.asString
                }
            }
            BepaidResponse(
                ResponseStatus.getStatus(status),
                message,
                cardToken,
                url
            )
        } else {
            BepaidResponse()
        }
    }
}