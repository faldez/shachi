package com.faldez.shachi.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61, tokenizerArgs = ["tokenchars='"])
@Entity(tableName = "post_tag")
data class PostTag(
    @ColumnInfo(name = "post_id") val postId: Int,
    @ColumnInfo(name = "server_id") val serverId: Int,
    val tags: String,
)