package com.faldez.shachi.model

import android.util.Log
import com.faldez.shachi.model.response.DanbooruPost
import com.faldez.shachi.model.response.GelbooruPost
import com.faldez.shachi.model.response.GelbooruPostResponse
import com.faldez.shachi.model.response.MoebooruPost

fun GelbooruPostResponse.applyBlacklist(tags: String?): List<GelbooruPost>? {
    val blacklists = tags?.split(",")?.map {
        it.split(" ")
    }

    Log.d("PostPagingSource", "applyBlacklist $tags to $blacklists")

    if (blacklists.isNullOrEmpty()) {
        return this.posts?.post
    }

    var counter = 0
    val posts =  this.posts?.post?.filter { post ->
        blacklists.forEach { tags ->
            if (post.tags.split(" ").containsAll(tags)) {
                counter++
                return@filter false
            }
        }

        true
    }

    Log.d("PostPagingSource", "applyBlacklist $counter filtered")

    return posts
}

@JvmName("danbooruPostApplyBlacklist")
fun List<DanbooruPost>.applyBlacklist(tags: String?): List<DanbooruPost> {
    val blacklists = tags?.split(",")?.map {
        it.split(" ")
    }

    Log.d("PostPagingSource", "applyBlacklist $tags to $blacklists")

    if (blacklists.isNullOrEmpty()) {
        return this
    }

    var counter = 0
    val posts = this.filter { post ->
        blacklists.forEach { tags ->
            if (post.tagString.split(" ").containsAll(tags)) {
                counter++
                return@filter false
            }
        }

        true
    }

    Log.d("PostPagingSource", "applyBlacklist $counter filtered")

    return posts
}

@JvmName("moebooruPostApplyBlacklist")
fun List<MoebooruPost>.applyBlacklist(tags: String?): List<MoebooruPost> {
    val blacklists = tags?.split(",")?.map {
        it.split(" ")
    }

    Log.d("PostPagingSource", "applyBlacklist $tags to $blacklists")

    if (blacklists.isNullOrEmpty()) {
        return this
    }

    var counter = 0
    val posts = this.filter { post ->
        blacklists.forEach { tags ->
            if (post.tags.split(" ").containsAll(tags)) {
                counter++
                return@filter false
            }
        }

        true
    }

    Log.d("PostPagingSource", "applyBlacklist $counter filtered")

    return posts
}
