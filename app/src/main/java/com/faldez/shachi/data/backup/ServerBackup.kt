package com.faldez.shachi.data.backup

import com.faldez.shachi.data.model.ServerType
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ServerBackup(
    @ProtoNumber(1) val serverId: Int,
    @ProtoNumber(2) val type: ServerType,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val url: String,
    @ProtoNumber(5) val username: String?,
    @ProtoNumber(6) val password: String?,
    @ProtoNumber(7) val blacklistedTags: String? = null,
    @ProtoNumber(8) var selected: Boolean = false,
)