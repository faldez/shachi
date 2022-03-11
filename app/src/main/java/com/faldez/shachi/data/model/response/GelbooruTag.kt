package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.serializer.NumberAsBooleanTypeSerializer
import com.faldez.shachi.util.serializer.SingleObjectAsArraySerializer
import kotlinx.serialization.Serializable

@Serializable
data class GelbooruTagResponse(
    val tags: GelbooruTags?,
)

@Serializable
data class GelbooruTags(
//    @Serializable(SingleObjectAsArraySerializer::class)
    val tag: List<GelbooruTag>?,
    val type: String,
    val count: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null,
)

@Serializable
data class GelbooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    @Serializable(NumberAsBooleanTypeSerializer::class)
    val ambiguous: Boolean,
)