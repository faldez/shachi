package com.faldez.shachi.model

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import kotlinx.parcelize.Parcelize

enum class ServerType {
    Gelbooru,
    Danbooru
}

@Parcelize
@Entity(tableName = "server", indices = [Index(value = ["url"], unique = true)])
data class Server(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "server_id") val serverId: Int,
    val type: ServerType,
    val title: String,
    val url: String,
) : Parcelable

@Entity(
    tableName = "selected_server",
    indices = [Index(value = ["server_id"])],
    foreignKeys = [ForeignKey(onDelete = CASCADE,
        entity = Server::class,
        parentColumns = ["server_id"],
        childColumns = ["server_id"])]
)
data class SelectedServer(
    @PrimaryKey @ColumnInfo(name = "selected_server_id") val selectedServerId: Int = 0,
    @ColumnInfo(name = "server_id") val serverId: Int,
)

@DatabaseView("SELECT " +
        "server.*, " +
        "selected_server.server_id IS NOT NULL AS selected " +
        "FROM server " +
        "LEFT JOIN selected_server ON server.server_id = selected_server.server_id",
    viewName = "server_with_selected")
data class ServerWithSelected(
    @ColumnInfo(name = "server_id") val serverId: Int,
    val type: ServerType,
    val title: String,
    val url: String,
    var selected: Boolean,
)