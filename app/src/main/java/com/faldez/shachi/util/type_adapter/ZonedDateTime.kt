package com.faldez.shachi.util.type_adapter

import com.google.gson.*
import java.lang.reflect.Type
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ZonedDateTimeAdapter : JsonDeserializer<ZonedDateTime?>,
    JsonSerializer<ZonedDateTime?> {
    private val format: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss Z yyyy")

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ZonedDateTime? {
        return ZonedDateTime.parse(json?.asString, format)
    }

    override fun serialize(
        src: ZonedDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(src?.format(format)!!)
    }
}

class JsonDateTimeAdapter : JsonDeserializer<ZonedDateTime?>,
    JsonSerializer<ZonedDateTime?> {
    private val format: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ZonedDateTime? {
        return ZonedDateTime.parse(json?.asString, format)
    }

    override fun serialize(
        src: ZonedDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(src?.format(format)!!)
    }
}