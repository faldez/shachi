package com.faldez.shachi.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Category {
    General,
    Artist,
    Copyright,
    Character,
    Metadata,
    Deprecated,
    Unknown
}

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val name: String,
    @NonNull val type: Category,
)