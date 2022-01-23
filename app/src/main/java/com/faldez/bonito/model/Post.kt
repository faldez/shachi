package com.faldez.bonito.model

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

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
    val id: Int,
    val change: Int,
    val md5: String,
    val creatorId: Int?,
    val hasChildren: Boolean,
    val createdAt: ZonedDateTime?,
    val status: String,
    val source: String,
    val hasNotes: Boolean,
    val hasComments: Boolean,
) : Parcelable
