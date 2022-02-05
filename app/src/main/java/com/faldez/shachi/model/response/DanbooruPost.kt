package com.faldez.shachi.model.response

import android.os.Parcelable
import com.faldez.shachi.util.type_adapter.SingleObjectAsArrayTypeAdapter
import com.faldez.shachi.util.type_adapter.JsonDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class DanbooruPost(
    val id: Int?,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
    @SerializedName("uploader_id") val uploaderId: Int,
    val score: Int,
    val source: String,
    val md5: String?,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("last_comment_bumped_at") val LastCommentBumpedAt: ZonedDateTime?,
    val rating: String,
    @SerializedName("image_width") val imageWidth: Int,
    @SerializedName("image_height") val imageHeight: Int,
    @SerializedName("tag_string") val tagString: String,
    @SerializedName("fav_count") val favCount: Int,
    @SerializedName("file_ext") val fileExt: String,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("last_noted_at") val lastNotedAt: ZonedDateTime?,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("has_children") val hasChildren: Boolean,
    @SerializedName("approver_id") val approverId: Int?,
    @SerializedName("tag_count_general") val tagCountGeneral: Int,
    @SerializedName("tag_count_artist") val tagCountArtist: Int,
    @SerializedName("tag_count_character") val tagCountCharacter: Int,
    @SerializedName("tag_count_copyright") val tagCountCopyright: Int,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("up_score") val upScore: Int,
    @SerializedName("down_score") val downScore: Int,
    @SerializedName("is_pending") val isPending: Boolean,
    @SerializedName("is_flagged") val isFlagged: Boolean,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("tag_count") val tagCount: Int,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("updated_at") val updatedAt: ZonedDateTime?,
    @SerializedName("is_banned") val isBanned: Boolean,
    @SerializedName("pixiv_id") val pixivId: Int,
    @JsonAdapter(JsonDateTimeAdapter::class) @SerializedName("last_commented_at") val lastCommentedAt: ZonedDateTime?,
    @SerializedName("has_active_children") val hasActiveChildren: Boolean,
    @SerializedName("bit_flags") val bitFlags: Int,
    @SerializedName("tag_count_meta") val tagCountMeta: Int,
    @SerializedName("has_large") val hasLarge: Boolean,
    @SerializedName("has_visible_children") val hasVisibleChildren: Boolean,
    @SerializedName("tag_string_general") val tagStringGeneral: String,
    @SerializedName("tag_string_character") val tagStringCharacter: String,
    @SerializedName("tag_string_copyright") val tagStringCopyright: String,
    @SerializedName("tag_string_artist") val tagStringArtist: String,
    @SerializedName("tag_string_meta") val tagStringMeta: String,
    @SerializedName("file_url") val fileUrl: String?,
    @SerializedName("large_file_url") val largeFileUrl: String?,
    @SerializedName("preview_file_url") val previewFileUrl: String?,
) : Parcelable

