package com.faldez.bonito.model

import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.faldez.bonito.model.response.EmptyStringAsNullTypeAdapter
import com.faldez.bonito.model.response.ZonedDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
@Entity(primaryKeys = ["server_url", "post_id"], indices = [Index(value = ["favorite"])])
data class Post(
    val height: Int,
    val width: Int,
    val score: Int?,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "parent_id") val parentId: Int?,
    @ColumnInfo(name = "sample_url") val sampleUrl: String?,
    @ColumnInfo(name = "sample_width") val sampleWidth: Int?,
    @ColumnInfo(name = "sample_height") val sampleHeight: Int?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?,
    @ColumnInfo(name = "preview_width") val previewWidth: Int?,
    @ColumnInfo(name = "preview_height") val previewHeight: Int?,
    val rating: String,
    val tags: String,
    @ColumnInfo(name = "post_id") val postId: Int,
    @ColumnInfo(name = "server_url") val serverUrl: String,
    val change: Int,
    val md5: String,
    @ColumnInfo(name = "creator_id") val creatorId: Int?,
    @ColumnInfo(name = "has_children") val hasChildren: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime?,
    val status: String,
    val source: String,
    @ColumnInfo(name = "has_notes") val hasNotes: Boolean,
    @ColumnInfo(name = "has_comments") val hasComments: Boolean,
    val favorite: Boolean = false,
) : Parcelable
