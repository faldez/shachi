package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.util.serializer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class GelbooruPostResponse(
    val posts: GelbooruPosts? = null,
)

@Serializable
data class GelbooruPosts(
    @Serializable(with = GelbooruPostSerializer::class)
    val post: List<GelbooruPost>? = null,
    val count: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null,
)

@Serializable
data class GelbooruPost(
    val height: Int = 0,
    val width: Int = 0,
    @Serializable(EmptyStringAsIntNullTypeSerializer::class) val score: Int? = null,
    val title: String? = null,
    val directory: String? = null,
    val owner: String? = null,
    val image: String? = null,
    @Serializable(NumberAsBooleanTypeSerializer::class) val sample: Boolean? = false,
    @Serializable(NumberAsBooleanTypeSerializer::class) @JsonNames("post_locked") val postLocked: Boolean? = false,
    @JsonNames("file_url") val fileUrl: String = "",
    @Serializable(EmptyStringAsIntNullTypeSerializer::class) @JsonNames("parent_id") val parentId: Int? = null,
    @Serializable(EmptyStringAsStringNullTypeSerializer::class) @JsonNames("sample_url") val sampleUrl: String? = null,
    @JsonNames("sample_width") val sampleWidth: Int? = null,
    @JsonNames("sample_height") val sampleHeight: Int? = null,
    @Serializable(EmptyStringAsStringNullTypeSerializer::class) @JsonNames("preview_url") val previewUrl: String? = null,
    @JsonNames("preview_width") val previewWidth: Int? = null,
    @JsonNames("preview_height") val previewHeight: Int? = null,
    val rating: String = "",
    val tags: String = "",
    val id: Int = 0,
    val change: Int = 0,
    val md5: String = "",
    @JsonNames("creator_id") val creatorId: Int? = null,
    @JsonNames("has_children") val hasChildren: Boolean = false,
    @Serializable(ZonedDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime? = null,
    val status: String = "",
    val source: String = "",
    @JsonNames("has_notes") val hasNotes: Boolean = false,
    @JsonNames("has_comments") val hasComments: Boolean = false,
)

object GelbooruPostSerializer :
    SingleObjectAsArraySerializer<GelbooruPost>(GelbooruPost.serializer())