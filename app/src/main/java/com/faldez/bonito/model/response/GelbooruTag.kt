package com.faldez.bonito.model.response

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type


data class GelbooruTagResponse(
    val tags: GelbooruTags,
)

data class GelbooruTags(
    @JsonAdapter(SingleObjectAsArrayTypeAdapter::class)
    val tag: List<GelbooruTag>?,
    val type: String,
)

data class GelbooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    @JsonAdapter(NumberAsBooleanTypeAdapter::class)
    val ambiguous: Boolean,
)

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