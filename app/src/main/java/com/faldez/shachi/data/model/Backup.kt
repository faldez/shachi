package com.faldez.shachi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val servers: List<Server>?,
    val blacklistedTags: List<BlacklistedTag>?,
    val blacklistedTagsCrossRef: List<ServerBlacklistedTagCrossRef>?,
    val favorites: List<Post>?,
    val savedSearches: List<SavedSearch>?,
    val searchHistories: List<SearchHistory>?,
)