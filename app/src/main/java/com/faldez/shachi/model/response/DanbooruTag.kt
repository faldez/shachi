package com.faldez.shachi.model.response

import com.faldez.shachi.util.type_adapter.SingleObjectAsArrayTypeAdapter
import com.faldez.shachi.util.type_adapter.ZonedDateTimeAdapter
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.time.ZonedDateTime


data class DanbooruTagResponse(
    val tags: DanbooruTags?,
)

data class DanbooruTags(
    @JsonAdapter(SingleObjectAsArrayTypeAdapter::class)
    val tag: List<DanbooruTag>?,
    val type: String,
)

data class DanbooruTag(
    val id: Int,
    val name: String,
    @SerializedName("post_count") val postCount: Int,
    val category: Int,
    @JsonAdapter(ZonedDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime,
    @JsonAdapter(ZonedDateTimeAdapter::class) @SerializedName("updated_at") val updatedAt: ZonedDateTime,
    @SerializedName("is_locked") val isLocked: Boolean,
)



