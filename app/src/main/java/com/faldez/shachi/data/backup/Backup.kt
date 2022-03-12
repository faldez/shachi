package com.faldez.shachi.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@Serializable
data class Backup(
    @ProtoNumber(1) val servers: List<ServerBackup>?,
    @ProtoNumber(2) val favorites: List<FavoriteBackup>?,
    @ProtoNumber(3) val savedSearches: List<SavedSearchBackup>?,
    @ProtoNumber(4) val searchHistories: List<SearchHistoryBackup>?,
)