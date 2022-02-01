package com.faldez.shachi.model

import androidx.room.*

@Entity(tableName = "saved_search", foreignKeys = [ForeignKey(childColumns = ["server_id"],
    onDelete = ForeignKey.CASCADE,
    parentColumns = ["server_id"],
    entity = Server::class)])
data class SavedSearch(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "saved_search_id") val savedSearchId: Int = 0,
    val tags: String,
    @ColumnInfo(name = "saved_search_title") val savedSearchTitle: String,
    @ColumnInfo(name = "server_id") val serverId: Int,
)

data class SavedSearchServer(
    @Embedded val savedSearch: SavedSearch,
    @Relation(
        parentColumn = "server_id",
        entityColumn = "server_id"
    )
    val server: Server,
)

@Entity(tableName = "saved_search_server_cross_ref",
    primaryKeys = ["saved_search_id", "server_id"],
    foreignKeys = [ForeignKey(childColumns = ["blacklisted_tag_id"],
        onDelete = ForeignKey.CASCADE,
        parentColumns = ["blacklisted_tag_id"],
        entity = BlacklistedTag::class)])
data class SavedSearchServerCrossRef(
    @ColumnInfo(name = "saved_search_id") val savedSearchId: Int,
    @ColumnInfo(name = "server_id") val serverId: Int,
)