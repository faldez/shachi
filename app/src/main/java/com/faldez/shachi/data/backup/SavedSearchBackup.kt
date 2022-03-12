package com.faldez.shachi.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class SavedSearchBackup(
    @ProtoNumber(1) val id: Int = 0,
    @ProtoNumber(2) val tags: String,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val serverId: Int,
    @ProtoNumber(5) val order: Int,
    @ProtoNumber(6) val dateAdded: Long,
)