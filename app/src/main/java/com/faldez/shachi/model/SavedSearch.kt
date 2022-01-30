package com.faldez.shachi.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_search")
data class SavedSearch(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "saved_search_id") val savedSearchId: Int = 0,
    @Embedded val server: Server,
    val tags: String,
)