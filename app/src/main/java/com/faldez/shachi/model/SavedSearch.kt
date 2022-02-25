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
    val server: ServerView,
)