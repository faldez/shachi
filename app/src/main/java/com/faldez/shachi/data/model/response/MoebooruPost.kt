package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.serializer.TimestampDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.ZonedDateTime

@Serializable
data class MoebooruPost(
    val id: Int,
    val tags: String,
    @Serializable(TimestampDateTimeSerializer::class) @JsonNames("created_at") val createdAt: ZonedDateTime?,
    @JsonNames("creator_id") val creatorId: Int,
    val author: String,
    val change: Int,
    val source: String,
    val score: Int,
    val md5: String,
    @JsonNames("file_size") val fileSize: Int,
    @JsonNames("file_url") val fileUrl: String,
    @JsonNames("is_shown_in_index") val isShownInIndex: Boolean,
    @JsonNames("preview_url") val previewUrl: String,
    @JsonNames("preview_width") val previewWidth: Int,
    @JsonNames("preview_height") val previewHeight: Int,
    @JsonNames("actual_preview_width") val actualPreviewWidth: Int,
    @JsonNames("actual_preview_height") val actualPreviewHeight: Int,
    @JsonNames("sample_url") val sampleUrl: String,
    @JsonNames("sample_width") val sampleWidth: Int,
    @JsonNames("sample_height") val sampleHeight: Int,
    @JsonNames("sample_file_size") val sampleFileSize: Int,
    @JsonNames("jpeg_url") val jpegUrl: String,
    @JsonNames("jpeg_width") val jpegWidth: Int,
    @JsonNames("jpeg_height") val jpegHeight: Int,
    @JsonNames("jpeg_file_size") val jpegFileSize: Int,
    val rating: String,
    @JsonNames("has_children") val hasChildren: Boolean,
    @JsonNames("parent_id") val parentId: Int,
    val status: String,
    val width: Int,
    val height: Int,
    @JsonNames("is_held") val isHeld: Boolean,
    @JsonNames("frames_pending_string") val framesPendingString: String,
    @JsonNames("frames_pending") val framesPending: List<String>,
    @JsonNames("frames_string") val framesString: String,
    val frames: List<String>,
)