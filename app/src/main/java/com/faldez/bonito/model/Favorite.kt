package com.faldez.bonito.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["server_url", "post_id"], tableName = "favorite")
data class Favorite(
    @ColumnInfo(name = "server_url") val serverUrl: String,
    @ColumnInfo(name = "post_id") val postId: Int,
)