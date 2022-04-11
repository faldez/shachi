package com.faldez.shachi.data.model.response.danbooru

import com.faldez.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruComment(
    val id: Int = 0,
    @JsonNames("post_id") val postId: Int = 0,
    val body: String = "",
    val score: Int = 0,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime? = null,
    @Serializable(JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime? = null,
    @JsonNames("updater_id") val updaterId: Int = 0,
    @JsonNames("do_not_bump_post") val doNotBumpPost: Boolean = false,
    @JsonNames("is_deleted") val isDeleted: Boolean = false,
    @JsonNames("is_sticky") val isSticky: Boolean = false,
    val creator: DanbooruCommentCreator? = null,
)

@Serializable
data class DanbooruCommentCreator(
    val id: Int,
    val name: String,
)