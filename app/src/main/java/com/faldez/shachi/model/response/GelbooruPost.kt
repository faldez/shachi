package com.faldez.shachi.model.response

import android.os.Parcelable
import com.faldez.shachi.util.type_adapter.EmptyStringAsNullTypeAdapter
import com.faldez.shachi.util.type_adapter.SingleObjectAsArrayTypeAdapter
import com.faldez.shachi.util.type_adapter.ZonedDateTimeAdapter
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class GelbooruPostResponse(
    val posts: GelbooruPosts?,
)

data class GelbooruPosts(
    @JsonAdapter(SingleObjectAsArrayTypeAdapter::class)
    val post: List<GelbooruPost>?,
    val count: Int,
    val offset: Int,
)

@Parcelize
data class GelbooruPost(
    val height: Int,
    val width: Int,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) val score: Int?,
    @SerializedName("file_url") val fileUrl: String,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("sample_url") val sampleUrl: String?,
    @SerializedName("sample_width") val sampleWidth: Int?,
    @SerializedName("sample_height") val sampleHeight: Int?,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("preview_width") val previewWidth: Int?,
    @SerializedName("preview_height") val previewHeight: Int?,
    val rating: String,
    val tags: String,
    val id: Int,
    val change: Int,
    val md5: String,
    @SerializedName("creator_id") val creatorId: Int?,
    @SerializedName("has_children") val hasChildren: Boolean,
    @JsonAdapter(ZonedDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
    val status: String,
    val source: String,
    @SerializedName("has_notes") val hasNotes: Boolean,
    @SerializedName("has_comments") val hasComments: Boolean,
) : Parcelable
