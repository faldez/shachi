package com.faldez.shachi.data.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object JsonDateTimeSerializer : KSerializer<ZonedDateTime> {
    private val format: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val string = decoder.decodeString()
        return ZonedDateTime.parse(string, format)
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("JsonDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(format))
    }
}