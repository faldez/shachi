package com.faldez.shachi.data.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    private val format: DateTimeFormatter = DateTimeFormatter.ofPattern("eee MMM d HH:mm:ss Z yyyy")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZonedDateTimeSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val string = decoder.decodeString()
        return ZonedDateTime.parse(string, format)
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(format))
    }
}
