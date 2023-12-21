package com.begateway.mobilepayments.parser

import android.content.Context
import com.begateway.mobilepayments.models.network.response.BeGatewayResponse
import com.begateway.mobilepayments.models.network.response.ResponseStatus
import com.begateway.mobilepayments.sdk.PaymentSdk
import com.begateway.mobilepayments.sdk.extractCreditCardInfo
import com.begateway.mobilepayments.sdk.saveCreditCardData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type


class BeGatewayResponseParser(private val context: Context) : JsonDeserializer<BeGatewayResponse> {
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
                            //val saveData = extractCreditCardInfo(response)
                            //saveCreditCardData(extractCreditCardInfo(response))
                            val creditCardInfo = extractCreditCardInfo(response)
                            saveCreditCardData(context, creditCardInfo)
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

//    data class CreditCardInfo(
//        val brand: String?,
//        val last4: String?,
//        val token: String?,
//        val stamp: String?
//    )
//
//    private fun extractCreditCardInfo(response: JsonObject): CreditCardInfo {
//        var brand: String? = null
//        var last4: String? = null
//        var token: String? = null
//        var stamp: String? = null
//
//        val creditCard = response.getAsJsonObject("credit_card")
//        if (creditCard != null && !creditCard.isJsonNull) {
//            brand = getString("brand", creditCard)
//            last4 = getString("last_4", creditCard)
//            token = getString("token", creditCard)
//            stamp = getString("stamp", creditCard)
//        }
//
//        return CreditCardInfo(brand, last4, token, stamp)
//    }
//
//private fun saveCreditCardData(creditCardInfo: CreditCardInfo?) {
//    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
//        "Save_Card_Token",
//        Context.MODE_PRIVATE
//    )
//
//    val editor = sharedPreferences.edit()
//
//    // Получаем текущий массив из SharedPreferences
//    val existingListJson = sharedPreferences.getString("CreditCardList", null)
//    Log.d("saveCreditCardData", "КАРТА $existingListJson")
//    val existingList = mutableListOf<CreditCardInfo>()
//
//    existingListJson?.let {
//        try {
//            // Разбираем строку JSON
//            val jsonArray = JSONArray(it)
//            for (i in 0 until jsonArray.length()) {
//                val jsonObject = jsonArray.getJSONObject(i)
//                val brand = jsonObject.optString("brand", null)
//                val last4 = jsonObject.optString("last4", null)
//                val token = jsonObject.optString("token", null)
//                val stamp = jsonObject.optString("stamp", null)
//                existingList.add(CreditCardInfo(brand, last4, token, stamp))
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
//
//    // Проверяем наличие совпадений по полю "Stamp"
//    val isStampExist = existingList.any { it.stamp == creditCardInfo?.stamp }
//
//    if (!isStampExist) {
//        // Добавляем новый объект в список
//        if (creditCardInfo != null) {
//            existingList.add(creditCardInfo)
//
//            // Преобразуем список в JSON-строку и сохраняем в SharedPreferences
//            val newListJson = Gson().toJson(existingList)
//            editor.putString("CreditCardList", newListJson)
//
//            // Применяем изменения
//            editor.apply()
//        }
//    } else {
//        // Обработка совпадения (если необходимо)
//        Log.d("saveCreditCardData", "Совпадение по полю 'Stamp', объект не добавлен.")
//    }
//}

}





