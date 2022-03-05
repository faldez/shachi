package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.type_adapter.DateTimeAdapter
import com.faldez.shachi.util.type_adapter.EmptyStringAsNullTypeAdapter
import com.faldez.shachi.util.type_adapter.SingleObjectAsArrayTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class GelbooruCommentResponse(
    val comments: GelbooruComments?,
)

data class GelbooruComments(
    @JsonAdapter(SingleObjectAsArrayTypeAdapter::class)
    val comment: List<GelbooruComment>?,
)

data class GelbooruComment(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    val body: String,
    val creator: String,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) @SerializedName("creator_id") val creatorId: Int?,
    @JsonAdapter(DateTimeAdapter::class) @SerializedName("created_at") val createdAt: LocalDateTime?,
)