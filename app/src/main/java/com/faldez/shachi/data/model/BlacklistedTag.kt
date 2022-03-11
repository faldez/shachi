package com.faldez.shachi.data.model

import androidx.room.*
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "blacklisted_tag")
data class BlacklistedTag(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "blacklisted_tag_id") val blacklistedTagId: Int = 0,
    val tags: String,
)

@Serializable
@Entity(tableName = "server_blacklisted_tag_cross_ref",
    primaryKeys = ["server_id", "blacklisted_tag_id"],
    indices = [Index("blacklisted_tag_id")],
    foreignKeys = [ForeignKey(childColumns = ["blacklisted_tag_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        parentColumns = ["blacklisted_tag_id"],
        entity = BlacklistedTag::class)])
data class ServerBlacklistedTagCrossRef(
    @ColumnInfo(name = "server_id") val serverId: Int,
    @ColumnInfo(name = "blacklisted_tag_id") val blacklistedTagId: Int,
)

data class BlacklistedTagWithServer(
    @Embedded val blacklistedTag: BlacklistedTag,
    @Relation(
        parentColumn = "blacklisted_tag_id",
        entityColumn = "server_id",
        associateBy = Junction(ServerBlacklistedTagCrossRef::class)
    )
    val servers: List<Server>,
)