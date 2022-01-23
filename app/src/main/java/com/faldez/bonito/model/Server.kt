package com.faldez.bonito.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ServerType {
    Gelbooru,
    Danbooru
}

@Entity
data class Server(
    @PrimaryKey val uid: Int = 0,
    val type: ServerType,
    val title: String,
    val url: String,
)
