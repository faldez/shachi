package com.faldez.shachi.data.util.serializer

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object NumberAsBooleanTypeSerializer : JsonTransformingSerializer<Boolean>(Boolean.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        require(element is JsonPrimitive)
        try {
            return JsonPrimitive(element.int > 0)
        } catch (e: Exception) {

        }

        try {
            return JsonPrimitive(element.boolean)
        } catch (e: Exception) {

        }

        return JsonPrimitive(false)
    }
}