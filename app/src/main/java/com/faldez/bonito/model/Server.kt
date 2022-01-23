package com.faldez.bonito.model

import androidx.room.*

enum class ServerType {
    Gelbooru,
    Danbooru
}

@Entity
data class Server(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "server_id") val serverId: Int = 0,
    val type: ServerType,
    val title: String,
    val url: String,
)

@Entity(tableName = "selected_server")
data class SelectedServer(
    @PrimaryKey @ColumnInfo(name = "selected_server_id") val selectedServerId: Int = 0,
    @ColumnInfo(name = "server_id") val serverId: Int,
)

@DatabaseView("SELECT " +
        "server.*, " +
        "selected_server.selected_server_id IS NOT NULL AS selected " +
        "FROM server " +
        "LEFT JOIN selected_server ON server.server_id = selected_server.server_id")
data class ServerWithSelected(
    @ColumnInfo(name = "server_id") val serverId: Int,
    val type: ServerType,
    val title: String,
    val url: String,
    var selected: Boolean,
)