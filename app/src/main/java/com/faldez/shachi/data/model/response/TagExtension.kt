package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.model.Category
import com.faldez.shachi.data.model.Tag
import com.faldez.shachi.data.model.TagDetail
import com.faldez.shachi.data.model.response.danbooru.DanbooruTag
import com.faldez.shachi.data.model.response.moebooru.MoebooruTag


fun GelbooruTagResponse.mapToTagDetails() =
    this.tags?.tag?.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = parseTag(it.type)
        )
    }

fun GelbooruTagResponse.mapToTags(serverId: Int) =
    this.tags?.tag?.map {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
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

fun GelbooruTagResponse.mapToTag(serverId: Int) =
    this.tags?.tag?.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
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
fun List<DanbooruTag>.mapToTags(serverId: Int) =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.category),
            serverId = serverId
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
fun List<DanbooruTag>.mapToTag(serverId: Int) =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.category),
            serverId = serverId
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
fun List<MoebooruTag>.mapToTags(serverId: Int) =
    this.map {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
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
fun List<MoebooruTag>.mapToTag(serverId: Int) =
    this.firstOrNull()?.let {
        Tag(
            name = it.name,
            type = parseTag(it.type),
            serverId = serverId
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