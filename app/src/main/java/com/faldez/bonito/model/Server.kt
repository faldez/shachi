package com.faldez.bonito.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

enum class ServerType {
    Gelbooru,
    Danbooru
}

@Parcelize
@Entity(tableName = "server", indices = [Index(value = ["url"], unique = true)])
data class Server(
    val type: ServerType,
    val title: String,
    @PrimaryKey val url: String,
) : Parcelable

@Entity(tableName = "selected_server", indices = [Index(value = ["server_url"])])
data class SelectedServer(
    @PrimaryKey @ColumnInfo(name = "selected_server_id") val selectedServerId: Int = 0,
    @ColumnInfo(name = "server_url") val serverUrl: String,
)

@DatabaseView("SELECT " +
        "server.*, " +
        "selected_server.server_url IS NOT NULL AS selected " +
        "FROM server " +
        "LEFT JOIN selected_server ON server.url = selected_server.server_url",
    viewName = "server_with_selected")
data class ServerWithSelected(
    val type: ServerType,
    val title: String,
    val url: String,
    var selected: Boolean,
)