package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.util.serializer.NumberAsBooleanTypeSerializer
import com.faldez.shachi.data.util.serializer.SingleObjectAsArraySerializer
import kotlinx.serialization.Serializable

@Serializable
data class GelbooruTagResponse(
    val tags: GelbooruTags? = null,
)

@Serializable
data class GelbooruTags(
    @Serializable(with = GelbooruTagSerializer::class)
    val tag: List<GelbooruTag>? = null,
    val type: String = "",
    val count: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null,
)

@Serializable
data class GelbooruTag(
    val id: Int = 0,
    val name: String = "",
    val count: Int = 0,
    val type: Int = 0,
    @Serializable(NumberAsBooleanTypeSerializer::class)
    val ambiguous: Boolean = false,
)

object GelbooruTagSerializer : SingleObjectAsArraySerializer<GelbooruTag>(GelbooruTag.serializer())