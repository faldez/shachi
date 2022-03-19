package com.faldez.shachi.data.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

open class SingleObjectAsArraySerializer<T>(serializer: KSerializer<T>) :
    JsonTransformingSerializer<List<T>>(ListSerializer(serializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonObject) {
            JsonArray(listOf(element))
        } else {
            element
        }
    }
}