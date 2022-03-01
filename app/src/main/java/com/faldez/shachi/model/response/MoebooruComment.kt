package com.faldez.shachi.model.response

import com.faldez.shachi.util.type_adapter.JsonDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class MoebooruComment(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    val body: String,
    val creator: String,
    @SerializedName("creator_id") val creatorId: Int,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
)