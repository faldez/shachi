package com.faldez.shachi.util.type_adapter

import com.google.gson.*
import java.lang.reflect.Type

class NumberAsBooleanTypeAdapter<T>  // Let Gson instantiate it itself
private constructor() : JsonDeserializer<T?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): T? {
        if (jsonElement.isJsonPrimitive) {
            val jsonPrimitive = jsonElement.asJsonPrimitive
            if (jsonPrimitive.isNumber && jsonPrimitive.asNumber == 0) {
                return context.deserialize(JsonPrimitive(false), type)
            }
            return context.deserialize(JsonPrimitive(true), type)
        }
        return context.deserialize(jsonElement, type)
    }
}