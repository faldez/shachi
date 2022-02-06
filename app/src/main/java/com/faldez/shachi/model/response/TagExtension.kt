package com.faldez.shachi.model.response

import com.faldez.shachi.model.Tag
import com.faldez.shachi.model.TagDetail


fun GelbooruTagResponse.mapToTagDetails() =
    this.tags?.tag?.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = it.type
        )
    }

fun GelbooruTagResponse.mapToTags() =
    this.tags?.tag?.map {
        Tag(
            name = it.name,
            type = it.type
        )
    }

fun GelbooruTagResponse.mapToTagDetail() =
    this.tags?.tag?.first()?.let {
        TagDetail(
            name = it.name,
            count = it.count,
            type = it.type
        )
    }

fun GelbooruTagResponse.mapToTag() =
    this.tags?.tag?.first()?.let {
        Tag(
            name = it.name,
            type = it.type
        )
    }

@JvmName("danbooruMapToTagDetails")
fun List<DanbooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.postCount,
            type = it.category
        )
    }

@JvmName("danbooruMapToTags")
fun List<DanbooruTag>.mapToTags() =
    this.map {
        Tag(
            name = it.name,
            type = it.category
        )
    }

@JvmName("danbooruMapToTagDetail")
fun List<DanbooruTag>.mapToTagDetail() =
    this.first().let {
        TagDetail(
            name = it.name,
            count = it.postCount,
            type = it.category
        )
    }

@JvmName("danbooruMapToTag")
fun List<DanbooruTag>.mapToTag() =
    this.first().let {
        Tag(
            name = it.name,
            type = it.category
        )
    }

@JvmName("moebooruMapToTagDetails")
fun List<MoebooruTag>.mapToTagDetails() =
    this.map {
        TagDetail(
            name = it.name,
            count = it.count,
            type = it.type
        )
    }

@JvmName("moebooruMapToTags")
fun List<MoebooruTag>.mapToTags() =
    this.map {
        Tag(
            name = it.name,
            type = it.type
        )
    }

@JvmName("moebooruMapToTagDetail")
fun List<MoebooruTag>.mapToTagDetail() =
    this.first().let {
        TagDetail(
            name = it.name,
            count = it.count,
            type = it.type
        )
    }

@JvmName("moebooruMapToTag")
fun List<MoebooruTag>.mapToTag() =
    this.first().let {
        Tag(
            name = it.name,
            type = it.type
        )
    }