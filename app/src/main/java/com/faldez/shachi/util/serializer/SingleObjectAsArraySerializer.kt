package com.faldez.shachi.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SingleObjectAsArraySerializer<T>(private val dataSerializer: KSerializer<T>) :
    KSerializer<List<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        return listOf(dataSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: List<T>) {
        dataSerializer.serialize(encoder, value[0])
    }
}