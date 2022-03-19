package com.faldez.shachi.data.model.response.danbooru

import com.faldez.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruTag(
    val id: Int,
    val name: String,
    @JsonNames("post_count") val postCount: Int,
    val category: Int,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime,
    @JsonNames("is_locked") val isLocked: Boolean,
)



