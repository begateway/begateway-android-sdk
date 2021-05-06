package com.begateway.mobilepayments.parser

import androidx.annotation.Keep
import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

@Keep
class CustomSerializer<T : AdditionalFields> : JsonSerializer<T> {
    override fun serialize(
        src: T,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        src::class.java.declaredFields.forEach { field ->
            val serializedName = field.getAnnotation(SerializedName::class.java).value
            field.isAccessible = true
            jsonObject.add(serializedName, context.serialize(field[src]))
        }
        src.fields
            .map { it.entrySet() }
            .forEach { entrySet ->
                entrySet.forEach { (key, value) -> jsonObject.add(key, value) }
            }
        return jsonObject
    }
}