package com.faldez.shachi.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class SearchHistoryBackup(
    @ProtoNumber(1) val id: Int,
    @ProtoNumber(2) val serverId: Int,
    @ProtoNumber(3) val tags: String,
    @ProtoNumber(4) val createdAt: Long,
)