package com.begateway.mobilepayments

import com.begateway.mobilepayments.models.network.AdditionalFields
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Field
import java.lang.reflect.Type

class CustomSerializer<T : AdditionalFields> : JsonSerializer<T> {
    private val gson = Gson()
    override fun serialize(
        src: T,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = gson.toJsonTree(src) as JsonObject
        src::class.java.declaredFields.forEach { field ->
            if (AdditionalFields::class.java.isAssignableFrom(field.type)) {
                val serializedName = field.getAnnotation(SerializedName::class.java).value
                jsonObject.remove(serializedName)
                field.isAccessible = true
                jsonObject.add(serializedName, context.serialize(field[src]))
            }
        }
        for (customElement in src.fields) {
            for ((key, value) in customElement.entrySet()) {
                jsonObject.add(key, value)
            }
        }
        return jsonObject
    }
}