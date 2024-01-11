package com.begateway.mobilepayments.sdk

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONException

data class SaveCardToken(
    val brand: String?,
    val last4: String?,
    val token: String?,
    val stamp: String?
)

fun extractCreditCardInfo(response: JsonObject): SaveCardToken {
    var brand: String? = null
    var last4: String? = null
    var token: String? = null
    var stamp: String? = null

    val creditCard = response.getAsJsonObject("credit_card")
    if (creditCard != null && !creditCard.isJsonNull) {
        brand = getString("brand", creditCard)
        last4 = getString("last_4", creditCard)
        token = getString("token", creditCard)
        stamp = getString("stamp", creditCard)
    }

    return SaveCardToken(brand, last4, token, stamp)
}

fun saveCreditCardData(context: Context, creditCardInfo: SaveCardToken?) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "Save_Card_Token",
        Context.MODE_PRIVATE
    )

    val editor = sharedPreferences.edit()

    // Получаем текущий массив из SharedPreferences
    val existingListJson = sharedPreferences.getString("CreditCardList", null)
    Log.d("saveCreditCardData", "КАРТА $existingListJson")
    val existingList = mutableListOf<SaveCardToken>()

    existingListJson?.let {
        try {
            // Разбираем строку JSON
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val brand = jsonObject.optString("brand")
                val last4 = jsonObject.optString("last4")
                val token = jsonObject.optString("token")
                val stamp = jsonObject.optString("stamp")
                existingList.add(SaveCardToken(brand, last4, token, stamp))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    // Проверяем наличие совпадений по полю "Stamp"
    val isStampExist = existingList.any { it.stamp == creditCardInfo?.stamp }
        // Добавляем новый объект в список
        if (!isStampExist && creditCardInfo?.last4?.length != null) {
            existingList.add(creditCardInfo)

            // Преобразуем список в JSON-строку и сохраняем в SharedPreferences
            val newListJson = Gson().toJson(existingList)
            editor.putString("CreditCardList", newListJson)

            // Применяем изменения
            editor.apply()
        } else {
        // Обработка совпадения (если необходимо)
        Log.d("saveCreditCardData", "Совпадение по полю 'Stamp', объект не добавлен.")
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
