package com.faldez.shachi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "blacklisted_tag")
data class BlacklistedTag(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "blacklisted_tag_id") val blacklistedTagId: Int = 0,
    val tags: String,
)

@Entity(tableName = "server_blacklisted_tag_cross_ref",
    primaryKeys = ["server_id", "blacklisted_tag_id"])
data class ServerBlacklistedTagCrossRef(
    @ColumnInfo(name = "server_id") val serverId: Int,
    @ColumnInfo(name = "blacklisted_tag_id") val blacklistedTagId: Int,
)