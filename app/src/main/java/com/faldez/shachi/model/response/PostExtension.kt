package com.faldez.shachi.model.response

import androidx.core.os.LocaleListCompat
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@JvmName("gelbooruMapToPost")
fun List<GelbooruPost>.mapToPost(serverId: Int): List<Post> {
    return this.map { post ->
        Post(
            height = post.height,
            width = post.width,
            score = post.score,
            fileUrl = post.fileUrl,
            parentId = post.parentId,
            sampleUrl = post.sampleUrl,
            sampleWidth = post.sampleWidth,
            sampleHeight = post.sampleHeight,
            previewUrl = post.previewUrl,
            previewWidth = post.previewWidth,
            previewHeight = post.previewHeight,
            rating = parseRating(post.rating),
            tags = post.tags,
            postId = post.id,
            serverId = serverId,
            change = post.change,
            md5 = post.md5,
            creatorId = post.creatorId,
            hasChildren = post.hasChildren,
            createdAt = post.createdAt?.format(DateTimeFormatter.ofPattern("cccc, dd MMMM y hh:mm:ss a",
                LocaleListCompat.getDefault().get(0))),
            status = post.status,
            source = post.source,
            hasNotes = post.hasNotes,
            hasComments = post.hasComments,
        )
    }
}

@JvmName("danbooruMapToPost")
fun List<DanbooruPost>.mapToPost(serverId: Int): List<Post> {
    return this.mapNotNull { post ->
        if (post.id == null) null
        else Post(
            height = post.imageHeight,
            width = post.imageWidth,
            score = post.score,
            fileUrl = post.fileUrl ?: "",
            parentId = post.parentId,
            sampleUrl = post.largeFileUrl,
            sampleWidth = null,
            sampleHeight = null,
            previewUrl = post.previewFileUrl,
            previewWidth = null,
            previewHeight = null,
            rating = parseRating(post.rating),
            tags = post.tagString,
            postId = post.id ?: 0,
            serverId = serverId,
            change = 0,
            md5 = post.md5 ?: "",
            creatorId = null,
            hasChildren = post.hasChildren,
            createdAt = post.createdAt?.format(DateTimeFormatter.ofPattern("cccc, dd MMMM y hh:mm:ss a",
                LocaleListCompat.getDefault().get(0))),
            status = "",
            source = post.source,
            hasNotes = false,
            hasComments = false,
        )
    }
}

@JvmName("moebooruMapToPost")
fun List<MoebooruPost>.mapToPost(serverId: Int): List<Post> {
    return this.map { post ->
        Post(
            height = post.height,
            width = post.width,
            score = post.score,
            fileUrl = post.fileUrl,
            parentId = post.parentId,
            sampleUrl = post.fileUrl,
            sampleWidth = null,
            sampleHeight = null,
            previewUrl = post.previewUrl,
            previewWidth = null,
            previewHeight = null,
            rating = parseRating(post.rating),
            tags = post.tags,
            postId = post.id,
            serverId = serverId,
            change = 0,
            md5 = post.md5,
            creatorId = null,
            hasChildren = post.hasChildren,
            createdAt = post.createdAt?.format(DateTimeFormatter.ofPattern("cccc, dd MMMM y hh:mm:ss a",
                LocaleListCompat.getDefault().get(0))),
            status = "",
            source = post.source,
            hasNotes = false,
            hasComments = false,
        )
    }
}

fun GelbooruPostResponse.mapToPost(serverId: Int): List<Post>? {
    return this.posts?.post?.map { post ->
        Post(
            height = post.height,
            width = post.width,
            score = post.score,
            fileUrl = post.fileUrl,
            parentId = post.parentId,
            sampleUrl = post.sampleUrl,
            sampleWidth = post.sampleWidth,
            sampleHeight = post.sampleHeight,
            previewUrl = post.previewUrl,
            previewWidth = post.previewWidth,
            previewHeight = post.previewHeight,
            rating = parseRating(post.rating),
            tags = post.tags,
            postId = post.id,
            serverId = serverId,
            change = post.change,
            md5 = post.md5,
            creatorId = post.creatorId,
            hasChildren = post.hasChildren,
            createdAt = post.createdAt?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)),
            status = post.status,
            source = post.source,
            hasNotes = post.hasNotes,
            hasComments = post.hasComments,
        )
    }
}

fun parseRating(rating: String): Rating =
    when (rating.lowercase()) {
        "s" -> Rating.Safe
        "safe" -> Rating.Safe
        "e" -> Rating.Explicit
        "explicit" -> Rating.Explicit
        "q" -> Rating.Questionable
        "questionable" -> Rating.Questionable
        else -> throw IllegalAccessException()
    }