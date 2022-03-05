package com.faldez.shachi.data.model.response

import com.faldez.shachi.util.type_adapter.TimestampDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime

data class MoebooruPost(
    val id: Int,
    val tags: String,
    @JsonAdapter(TimestampDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
    @SerializedName("creator_id") val creatorId: Int,
    val author: String,
    val change: Int,
    val source: String,
    val score: Int,
    val md5: String,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("is_shown_in_index") val isShownInIndex: Boolean,
    @SerializedName("preview_url") val previewUrl: String,
    @SerializedName("preview_width") val previewWidth: Int,
    @SerializedName("preview_height") val previewHeight: Int,
    @SerializedName("actual_preview_width") val actualPreviewWidth: Int,
    @SerializedName("actual_preview_height") val actualPreviewHeight: Int,
    @SerializedName("sample_url") val sampleUrl: String,
    @SerializedName("sample_width") val sampleWidth: Int,
    @SerializedName("sample_height") val sampleHeight: Int,
    @SerializedName("sample_file_size") val sampleFileSize: Int,
    @SerializedName("jpeg_url") val jpegUrl: String,
    @SerializedName("jpeg_width") val jpegWidth: Int,
    @SerializedName("jpeg_height") val jpegHeight: Int,
    @SerializedName("jpeg_file_size") val jpegFileSize: Int,
    val rating: String,
    @SerializedName("has_children") val hasChildren: Boolean,
    @SerializedName("parent_id") val parentId: Int,
    val status: String,
    val width: Int,
    val height: Int,
    @SerializedName("is_held") val isHeld: Boolean,
    @SerializedName("frames_pending_string") val framesPendingString: String,
    @SerializedName("frames_pending") val framesPending: List<String>,
    @SerializedName("frames_string") val framesString: String,
    val frames: List<String>,
)