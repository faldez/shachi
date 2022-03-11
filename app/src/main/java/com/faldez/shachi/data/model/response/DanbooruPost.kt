package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruPost(
    val id: Int? = null,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime?,
    @JsonNames("uploader_id") val uploaderId: Int,
    val score: Int,
    val source: String,
    val md5: String? = null,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_comment_bumped_at") val LastCommentBumpedAt: ZonedDateTime?,
    val rating: String,
    @JsonNames("image_width") val imageWidth: Int,
    @JsonNames("image_height") val imageHeight: Int,
    @JsonNames("tag_string") val tagString: String,
    @JsonNames("fav_count") val favCount: Int,
    @JsonNames("file_ext") val fileExt: String,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_noted_at") val lastNotedAt: ZonedDateTime?,
    @JsonNames("parent_id") val parentId: Int?,
    @JsonNames("has_children") val hasChildren: Boolean,
    @JsonNames("approver_id") val approverId: Int?,
    @JsonNames("tag_count_general") val tagCountGeneral: Int,
    @JsonNames("tag_count_artist") val tagCountArtist: Int,
    @JsonNames("tag_count_character") val tagCountCharacter: Int,
    @JsonNames("tag_count_copyright") val tagCountCopyright: Int,
    @JsonNames("file_size") val fileSize: Int,
    @JsonNames("up_score") val upScore: Int,
    @JsonNames("down_score") val downScore: Int,
    @JsonNames("is_pending") val isPending: Boolean,
    @JsonNames("is_flagged") val isFlagged: Boolean,
    @JsonNames("is_deleted") val isDeleted: Boolean,
    @JsonNames("tag_count") val tagCount: Int,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime?,
    @JsonNames("is_banned") val isBanned: Boolean,
    @JsonNames("pixiv_id") val pixivId: Int?,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_commented_at") val lastCommentedAt: ZonedDateTime?,
    @JsonNames("has_active_children") val hasActiveChildren: Boolean,
    @JsonNames("bit_flags") val bitFlags: Int,
    @JsonNames("tag_count_meta") val tagCountMeta: Int,
    @JsonNames("has_large") val hasLarge: Boolean? = false,
    @JsonNames("has_visible_children") val hasVisibleChildren: Boolean,
    @JsonNames("tag_string_general") val tagStringGeneral: String,
    @JsonNames("tag_string_character") val tagStringCharacter: String,
    @JsonNames("tag_string_copyright") val tagStringCopyright: String,
    @JsonNames("tag_string_artist") val tagStringArtist: String,
    @JsonNames("tag_string_meta") val tagStringMeta: String,
    @JsonNames("file_url") val fileUrl: String? = null,
    @JsonNames("large_file_url") val largeFileUrl: String? = null,
    @JsonNames("preview_file_url") val previewFileUrl: String? = null,
)

