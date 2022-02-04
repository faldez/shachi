package com.faldez.shachi.util.type_adapter

import com.google.gson.*
import java.lang.reflect.Type

class SingleObjectAsArrayTypeAdapter<T>  // Let Gson instantiate it itself
private constructor() : JsonDeserializer<T?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): T? {
        if (jsonElement.isJsonObject) {
            val array = JsonArray()
            array.add(jsonElement)
            return context.deserialize(array, type)
        }
        return context.deserialize(jsonElement, type)
    }
}