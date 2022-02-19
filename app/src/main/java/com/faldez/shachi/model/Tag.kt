package com.faldez.shachi.model

import android.os.Parcelable
import android.util.Log
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

enum class Category {
    General,
    Artist,
    Copyright,
    Character,
    Metadata,
    Deprecated,
    Unknown
}

enum class Modifier(val prefix: String) {
    Minus("-"),
    Tilde("~"),
    Wildcard("*")
}

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val name: String,
    @NonNull val type: Category,
) {
    override operator fun equals(other: Any?): Boolean {
        return when (other) {
            is Tag -> {
                val regex = Regex("^[-\\~]")
                val thisName = this.name.replaceFirst(regex, "")
                val otherName = other.name.replaceFirst(regex, "")
                Log.d("Tag/equals", "Tag $thisName == $otherName")
                thisName == otherName && this.type == other.type
            }
            is String -> {
                val regex = Regex("^[-\\~]")
                val thisName = this.name.replaceFirst(regex, "")
                val otherName = other.replaceFirst(regex, "")
                Log.d("Tag/equals", "String $thisName == $otherName")
                thisName == otherName
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        Log.d("Tag/hashCode", "hashCode")
        return Objects.hashCode(this.copy(name = name.replaceFirst(Regex("^[-\\~]"), "")))
    }
}

/*
Tag with post count and excluded status, only to be used in search
*/
@Parcelize
data class TagDetail(
    val name: String,
    val count: Int? = null,
    val type: Category = Category.Unknown,
    val modifier: Modifier? = null,
) : Parcelable {
    override fun toString(): String {
        return when (modifier) {
            null -> name
            else -> "${modifier.prefix}$name"
        }
    }

    companion object {
        fun fromName(name: String): TagDetail {
            val tag = name.replaceFirst(Regex("^[-~*]"), "")
            val modifier = when (name.firstOrNull()) {
                '~' -> Modifier.Tilde
                '-' -> Modifier.Minus
                '*' -> Modifier.Wildcard
                null -> throw IllegalAccessException()
                else -> null
            }
            return TagDetail(name = tag, modifier = modifier)
        }
    }
}

fun List<Tag>.mapToTagDetails(): List<TagDetail> = this.map {
    TagDetail(name = it.name, type = it.type)
}

fun List<TagDetail>.mapToTags(): List<Tag> = this.map { Tag(name = it.name, type = it.type) }