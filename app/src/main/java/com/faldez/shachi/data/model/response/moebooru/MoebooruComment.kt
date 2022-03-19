package com.faldez.shachi.data.model.response.moebooru

import com.faldez.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class MoebooruComment(
    val id: Int,
    @JsonNames("post_id") val postId: Int,
    val body: String,
    val creator: String,
    @JsonNames("creator_id") val creatorId: Int,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime?,
)