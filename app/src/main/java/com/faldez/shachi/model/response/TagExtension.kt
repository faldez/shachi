package com.faldez.shachi.model.response

import com.faldez.shachi.model.Category
import com.faldez.shachi.model.Tag
import com.faldez.shachi.model.TagDetail


fun GelbooruTagResponse.mapToTagDetails() =
    this.tags?.tag?.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

fun GelbooruTagResponse.mapToTags() =
    this.tags?.tag?.map {
        Tag(
            name = it.name,
            type = parseTag(it.type)
        )
    }

fun GelbooruTagResponse.mapToTagDetail() =
    this.tags?.tag?.firstOrNull()?.let {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

fun GelbooruTagResponse.mapToTag() =
    this.tags?.tag?.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type)
        )
    }

@JvmName("danbooruMapToTagDetails")
fun List<DanbooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.postCount,
            type = parseTag(it.category)
        )
    }

@JvmName("danbooruMapToTags")
fun List<DanbooruTag>.mapToTags() =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.category)
        )
    }

@JvmName("danbooruMapToTagDetail")
fun List<DanbooruTag>.mapToTagDetail() =
    this.firstOrNull()?.let {
        TagDetail(
            name = it.name,
            count = it.postCount,
            type = parseTag(it.category)
        )
    }

@JvmName("danbooruMapToTag")
fun List<DanbooruTag>.mapToTag() =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.category)
        )
    }

@JvmName("moebooruMapToTagDetails")
fun List<MoebooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

@JvmName("moebooruMapToTags")
fun List<MoebooruTag>.mapToTags() =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.type)
        )
    }

@JvmName("moebooruMapToTagDetail")
fun List<MoebooruTag>.mapToTagDetail() =
    this.firstOrNull()?.let {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

@JvmName("moebooruMapToTag")
fun List<MoebooruTag>.mapToTag() =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type)
        )
    }

fun parseTag(type: Int) = when (type) {
    0 -> Category.General
    1 -> Category.Artist
    3 -> Category.Copyright
    4 -> Category.Character
    5 -> Category.Metadata
    6 -> Category.Deprecated
    else -> throw IllegalAccessException("$type is not exist")
}