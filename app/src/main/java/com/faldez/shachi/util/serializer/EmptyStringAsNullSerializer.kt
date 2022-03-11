package com.faldez.shachi.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EmptyStringAsNullTypeSerializer : KSerializer<String?> {
    override fun deserialize(decoder: Decoder): String? {
        val string = decoder.decodeString()
        if (string.isNotEmpty()) {
            return string
        } else {
            return null
        }
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EmptyStringAsNullTypeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        value?.let { encoder.encodeString(it) }
    }

}