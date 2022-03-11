package com.faldez.shachi.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimestampDateTimeSerializer : KSerializer<ZonedDateTime> {
    private val format: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val long = decoder.decodeLong()
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(long), ZoneOffset.UTC)
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TimestampDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(format))
    }
}