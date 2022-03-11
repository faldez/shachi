package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruComment(
    val id: Int,
    @JsonNames("post_id") val postId: Int,
    val body: String,
    val score: Int,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime?,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime?,
    @JsonNames("updater_id") val updaterId: Int,
    @JsonNames("do_not_bump_post") val doNotBumpPost: Boolean,
    @JsonNames("is_deleted") val isDeleted: Boolean,
    @JsonNames("is_sticky") val isSticky: Boolean,
    val creator: DanbooruCommentCreator,
)

@Serializable
data class DanbooruCommentCreator(
    val id: Int,
    val name: String,
)