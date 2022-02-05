package com.faldez.shachi.model.response

import com.faldez.shachi.model.Tag


fun GelbooruTagResponse.mapToTags() =
    this.tags?.tag?.map {
        Tag(
            id = it.id,
            name = it.name,
            count = it.count,
            type = it.type,
            ambiguous = it.ambiguous
        )
    }

fun GelbooruTagResponse.mapToTag() =
    this.tags?.tag?.first()?.let {
        Tag(
            id = it.id,
            name = it.name,
            count = it.count,
            type = it.type,
            ambiguous = it.ambiguous
        )
    }

@JvmName("danbooruMapToTags")
fun List<DanbooruTag>.mapToTags() =
    this.map {
        Tag(
            id = it.id,
            name = it.name,
            count = it.postCount,
            type = it.category,
            ambiguous = false
        )
    }

@JvmName("danbooruMapToTag")
fun List<DanbooruTag>.mapToTag() =
    this.first().let {
        Tag(
            id = it.id,
            name = it.name,
            count = it.postCount,
            type = it.category,
            ambiguous = false
        )
    }

@JvmName("modesbooruMapToTags")
fun List<MoebooruTag>.mapToTags() =
    this.map {
        Tag(
            id = it.id,
            name = it.name,
            count = it.count,
            type = it.type,
            ambiguous = false
        )
    }

@JvmName("modesbooruMapToTag")
fun List<MoebooruTag>.mapToTag() =
    this.first().let {
        Tag(
            id = it.id,
            name = it.name,
            count = it.count,
            type = it.type,
            ambiguous = false
        )
    }