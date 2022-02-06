package com.faldez.shachi.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val name: String,
    val type: Int,
)

/*
Tag with post count and excluded status, only to be used in search
*/
@Parcelize
data class TagDetail(
    val name: String,
    val count: Int? = null,
    val type: Int,
    val excluded: Boolean = false,
) : Parcelable {
    override fun toString(): String {
        return if (excluded) {
            "-$name"
        } else {
            name
        }
    }

    companion object {
        fun fromName(name: String): TagDetail {
            return TagDetail(name = name, count = 0, type = 0)
        }
    }
}

fun List<Tag>.mapToTagDetails(): List<TagDetail> = this.map {
    TagDetail(name = it.name, type = it.type)
}

fun List<TagDetail>.mapToTags(): List<Tag> = this.map { Tag(name = it.name, type = it.type) }