package com.faldez.shachi.data.model.response.danbooru

import com.faldez.shachi.data.util.serializer.JsonDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class DanbooruPost(
    val id: Int? = null,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime? = null,
    @JsonNames("uploader_id") val uploaderId: Int = 0,
    val score: Int = 0,
    val source: String = "",
    val md5: String? = null,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_comment_bumped_at") val LastCommentBumpedAt: ZonedDateTime? = null,
    val rating: String = "",
    @JsonNames("image_width") val imageWidth: Int = 0,
    @JsonNames("image_height") val imageHeight: Int = 0,
    @JsonNames("tag_string") val tagString: String = "",
    @JsonNames("fav_count") val favCount: Int = 0,
    @JsonNames("file_ext") val fileExt: String = "",
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_noted_at") val lastNotedAt: ZonedDateTime? = null,
    @JsonNames("parent_id") val parentId: Int? = null,
    @JsonNames("has_children") val hasChildren: Boolean = false,
    @JsonNames("approver_id") val approverId: Int? = null,
    @JsonNames("tag_count_general") val tagCountGeneral: Int = 0,
    @JsonNames("tag_count_artist") val tagCountArtist: Int = 0,
    @JsonNames("tag_count_character") val tagCountCharacter: Int = 0,
    @JsonNames("tag_count_copyright") val tagCountCopyright: Int = 0,
    @JsonNames("file_size") val fileSize: Int = 0,
    @JsonNames("up_score") val upScore: Int = 0,
    @JsonNames("down_score") val downScore: Int = 0,
    @JsonNames("is_pending") val isPending: Boolean = false,
    @JsonNames("is_flagged") val isFlagged: Boolean = false,
    @JsonNames("is_deleted") val isDeleted: Boolean = false,
    @JsonNames("tag_count") val tagCount: Int = 0,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("updated_at") val updatedAt: ZonedDateTime? = null,
    @JsonNames("is_banned") val isBanned: Boolean = false,
    @JsonNames("pixiv_id") val pixivId: Int? = 0,
    @Serializable(with = JsonDateTimeSerializer::class) @JsonNames("last_commented_at") val lastCommentedAt: ZonedDateTime? = null,
    @JsonNames("has_active_children") val hasActiveChildren: Boolean = false,
    @JsonNames("bit_flags") val bitFlags: Int = 0,
    @JsonNames("tag_count_meta") val tagCountMeta: Int = 0,
    @JsonNames("has_large") val hasLarge: Boolean? = false,
    @JsonNames("has_visible_children") val hasVisibleChildren: Boolean = false,
    @JsonNames("tag_string_general") val tagStringGeneral: String = "",
    @JsonNames("tag_string_character") val tagStringCharacter: String = "",
    @JsonNames("tag_string_copyright") val tagStringCopyright: String = "",
    @JsonNames("tag_string_artist") val tagStringArtist: String = "",
    @JsonNames("tag_string_meta") val tagStringMeta: String = "",
    @JsonNames("file_url") val fileUrl: String? = null,
    @JsonNames("large_file_url") val largeFileUrl: String? = null,
    @JsonNames("preview_file_url") val previewFileUrl: String? = null,
)

