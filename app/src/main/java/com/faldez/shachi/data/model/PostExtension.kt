package com.faldez.shachi.data.model

import androidx.paging.PagingData
import androidx.paging.filter

fun PagingData<Post>.applyFilters(
    tags: String?,
    muteQuestionable: Boolean,
    muteExplicit: Boolean,
): PagingData<Post> {
    val blacklists = tags?.split(",")?.map {
        it.split(" ")
    }

    var counter = 0
    val posts = this.filter { post ->
        val isMute = when (post.rating) {
            Rating.Questionable -> muteQuestionable
            Rating.Explicit -> muteExplicit
            Rating.Safe -> false
        }

        if (isMute) {
            return@filter false
        } else {
            val postTags = post.tags.split(" ")
            blacklists?.forEach { tags ->
                if (postTags.containsAll(tags)) {
                    counter++
                    return@filter false
                }
            }
        }

        true
    }

    return posts
}