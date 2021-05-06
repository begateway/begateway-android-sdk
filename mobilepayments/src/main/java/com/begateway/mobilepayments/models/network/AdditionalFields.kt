package com.begateway.mobilepayments.models.network

import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

@Keep
open class AdditionalFields(
    @Transient
    open val fields: MutableList<JsonObject> = mutableListOf()
) {
    fun addCustomField(name: String, value: Number) {
        fields.add(JsonObject().apply { addProperty(name, value) })
    }

    fun addCustomField(name: String, value: String) {
        fields.add(JsonObject().apply { addProperty(name, value) })
    }

    fun addCustomField(name: String, value: Boolean) {
        fields.add(JsonObject().apply { addProperty(name, value) })
    }

    fun addCustomField(name: String, value: Char) {
        fields.add(JsonObject().apply { addProperty(name, value) })
    }

    @Throws(IllegalArgumentException::class)
    fun addCustomJsonElementFromString(jsonString: String) {
        try {
            val newElement = JsonParser().parse(jsonString)
            if (newElement.isJsonObject) {
                fields.add(newElement.asJsonObject)
            } else {
                throw IllegalArgumentException("argument is not JsonObject")
            }
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException(e.message)
        }
    }

    fun addCustomJsonObject(jsonObject: JsonObject) {
        fields.add(jsonObject)
    }
}