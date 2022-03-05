package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.type_adapter.JsonDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime

data class DanbooruComment(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    val body: String,
    val score: Int,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("updated_at") val updatedAt: ZonedDateTime?,
    @SerializedName("updater_id") val updaterId: Int,
    @SerializedName("do_not_bump_post") val doNotBumpPost: Boolean,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("is_sticky") val isSticky: Boolean,
    val creator: DanbooruCommentCreator,
)

data class DanbooruCommentCreator(
    val id: Int,
    val name: String,
)