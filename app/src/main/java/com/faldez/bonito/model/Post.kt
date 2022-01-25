package com.faldez.bonito.model

import android.os.Parcelable
import android.util.Log
import androidx.room.*
import com.faldez.bonito.model.response.EmptyStringAsNullTypeAdapter
import com.faldez.bonito.model.response.ZonedDateTimeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Parcelize
data class Post(
    val height: Int,
    val width: Int,
    val score: Int?,
    val fileUrl: String,
    val parentId: Int?,
    val sampleUrl: String?,
    val sampleWidth: Int?,
    val sampleHeight: Int?,
    val previewUrl: String?,
    val previewWidth: Int?,
    val previewHeight: Int?,
    val rating: String,
    val tags: String,
    val postId: Int,
    val serverUrl: String,
    val change: Int,
    val md5: String,
    val creatorId: Int?,
    val hasChildren: Boolean,
    val createdAt: String?,
    val status: String,
    val source: String,
    val hasNotes: Boolean,
    val hasComments: Boolean,
    var favorite: Boolean = false,
) : Parcelable