package com.faldez.shachi.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EmptyStringAsStringNullTypeSerializer : KSerializer<String?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EmptyStringAsStringNullTypeSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val string = decoder.decodeString()
        return string.ifEmpty {
            null
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        value?.let { encoder.encodeString(it) }
    }
}