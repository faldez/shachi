package com.faldez.shachi.model

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import kotlinx.parcelize.Parcelize

enum class ServerType {
    Gelbooru,
    Danbooru,
    Moebooru
}

@Parcelize
@Entity(tableName = "server", indices = [Index(value = ["url"], unique = true)])
data class Server(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "server_id") val serverId: Int,
    val type: ServerType,
    val title: String,
    val url: String,
    val username: String?,
    val password: String?,
) : Parcelable {
    fun toServerView(): ServerView {
        return  ServerView(
            serverId = serverId,
            type = type,
            title = title,
            url = url,
            username = username,
            password = password
        )
    }
}

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

@Parcelize
@DatabaseView("SELECT " +
        "server.*, " +
        "selected_server.server_id IS NOT NULL AS selected, " +
        "GROUP_CONCAT(blacklisted_tag.tags, ',') AS blacklisted_tags " +
        "FROM server " +
        "LEFT JOIN selected_server ON server.server_id = selected_server.server_id " +
        "LEFT JOIN server_blacklisted_tag_cross_ref ON server.server_id = server_blacklisted_tag_cross_ref.server_id " +
        "LEFT JOIN blacklisted_tag ON server_blacklisted_tag_cross_ref.blacklisted_tag_id = blacklisted_tag.blacklisted_tag_id " +
        "GROUP BY server.server_id",
    viewName = "server_view")
data class ServerView(
    @ColumnInfo(name = "server_id") val serverId: Int,
    val type: ServerType,
    val title: String,
    val url: String,
    val username: String?,
    val password: String?,
    @ColumnInfo(name = "blacklisted_tags") val blacklistedTags: String? = null,
    var selected: Boolean = false,
) : Parcelable {
    fun toServer(): Server {
        return Server(
            serverId = serverId,
            type = type,
            title = title,
            url = url,
            username = username,
            password = password
        )
    }
}