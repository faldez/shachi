package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.util.serializer.LocalDateTimeSerializer
import com.faldez.shachi.data.util.serializer.SingleObjectAsArraySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.LocalDateTime

@Serializable
data class GelbooruCommentResponse(
    val comments: GelbooruComments?,
)

@Serializable
data class GelbooruComments(
    @Serializable(with = GelbooruCommentSerializer::class)
    val comment: List<GelbooruComment>?,
)

@Serializable
data class GelbooruComment(
    val id: Int,
    @JsonNames("post_id") val postId: Int,
    val body: String,
    val creator: String,
    @JsonNames("creator_id") val creatorId: Int?,
    @Serializable(LocalDateTimeSerializer::class) @JsonNames("created_at") val createdAt: LocalDateTime?,
)

object GelbooruCommentSerializer :
    SingleObjectAsArraySerializer<GelbooruComment>(GelbooruComment.serializer())