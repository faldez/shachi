package com.faldez.shachi.data.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

enum class Category {
    General,
    Artist,
    Copyright,
    Character,
    Metadata,
    Deprecated,
    Unknown
}

@Entity(tableName = "tag",
    primaryKeys = ["name", "server_id"],
    indices = [Index("server_id")],
    foreignKeys = [ForeignKey(childColumns = ["server_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        parentColumns = ["server_id"],
        entity = Server::class)])
data class Tag(
    @NonNull val name: String,
    @NonNull val type: Category,
    @ColumnInfo(name = "server_id") val serverId: Int,
)