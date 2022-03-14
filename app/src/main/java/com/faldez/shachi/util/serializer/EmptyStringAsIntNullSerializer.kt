package com.faldez.shachi.util.serializer

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

object EmptyStringAsIntNullTypeSerializer :
    JsonTransformingSerializer<Int>(Int.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        require(element is JsonPrimitive)
        return if (element.isString) JsonPrimitive(0)
        else element
    }
}