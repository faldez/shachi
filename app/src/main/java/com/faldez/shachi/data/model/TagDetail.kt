package com.faldez.shachi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class Modifier(val symbol: String) {
    Minus("-"),
    Tilde("~"),
    Wildcard("*"),
}

const val modifierRegex = "^[-~*\\{]|\\}\$"

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
            else -> "${modifier.symbol}$name"
        }
    }

    companion object {
        fun fromName(name: String): TagDetail {
            val tag = name.replaceFirst(Regex(modifierRegex), "")
            val modifier = when (name.firstOrNull()) {
                '~' -> Modifier.Tilde
                '-' -> Modifier.Minus
                '*' -> Modifier.Wildcard
                null -> throw IllegalAccessException()
                else -> null
            }
            return TagDetail(name = tag, modifier = modifier)
        }

        fun fromTag(tag: Tag): TagDetail {
            return fromName(tag.name).copy(type = tag.type)
        }
    }
}

fun List<Tag>.mapToTagDetails(): List<TagDetail> = this.map {
    TagDetail(name = it.name, type = it.type)
}

fun List<TagDetail>.mapToTags(serverId: Int): List<Tag> =
    this.map { Tag(name = it.name, type = it.type, serverId = serverId) }