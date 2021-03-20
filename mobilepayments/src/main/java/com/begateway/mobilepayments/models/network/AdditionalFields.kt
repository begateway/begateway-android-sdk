package com.begateway.mobilepayments.models.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

open class AdditionalFields(
    @Transient
    open var fields: MutableList<JsonElement> = mutableListOf()
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

    fun addCustomJsonElementFromString(jsonString: String) {
        fields.add(JsonParser().parse(jsonString))
    }

    fun addCustomJsonElement(jsonElement: JsonElement) {
        fields.add(jsonElement)
    }
}