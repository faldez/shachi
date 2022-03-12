package com.faldez.shachi.data.backup

import com.faldez.shachi.data.model.Rating
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class FavoriteBackup(
    @ProtoNumber(1) val height: Int,
    @ProtoNumber(2) val width: Int,
    @ProtoNumber(3) val score: Int?,
    @ProtoNumber(4) val fileUrl: String,
    @ProtoNumber(5) val parentId: Int?,
    @ProtoNumber(6) val sampleUrl: String?,
    @ProtoNumber(7) val sampleWidth: Int?,
    @ProtoNumber(8) val sampleHeight: Int?,
    @ProtoNumber(9) val previewUrl: String?,
    @ProtoNumber(10) val previewWidth: Int?,
    @ProtoNumber(11) val previewHeight: Int?,
    @ProtoNumber(12) val rating: Rating,
    @ProtoNumber(13) val tags: String,
    @ProtoNumber(14) val postId: Int,
    @ProtoNumber(15) val serverId: Int,
    @ProtoNumber(16) val change: Int,
    @ProtoNumber(17) val md5: String,
    @ProtoNumber(18) val creatorId: Int?,
    @ProtoNumber(19) val hasChildren: Boolean,
    @ProtoNumber(20) val createdAt: String?,
    @ProtoNumber(21) val status: String,
    @ProtoNumber(22) val source: String,
    @ProtoNumber(23) val hasNotes: Boolean,
    @ProtoNumber(24) val hasComments: Boolean,
    @ProtoNumber(25) val postUrl: String,
    @ProtoNumber(26) val dateAdded: Long? = null,
)