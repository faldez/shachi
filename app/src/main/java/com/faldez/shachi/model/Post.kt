package com.faldez.shachi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Fts4
@Entity(tableName = "favorite")
@Parcelize
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
    @ColumnInfo(name = "created_at") val createdAt: String?,
    val status: String,
    val source: String,
    @ColumnInfo(name = "has_notes") val hasNotes: Boolean,
    @ColumnInfo(name = "has_comments") val hasComments: Boolean,
) : Parcelable {
    @Ignore
    var favorite: Boolean = false
}