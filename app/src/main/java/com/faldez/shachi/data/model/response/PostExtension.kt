package com.faldez.shachi.data.model.response

import androidx.core.os.LocaleListCompat
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.Rating
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.model.response.danbooru.DanbooruPost
import com.faldez.shachi.data.model.response.moebooru.MoebooruPost
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@JvmName("gelbooruMapToPost")
fun List<GelbooruPost>.mapToPost(server: Server): List<Post> {
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
            serverId = server.serverId,
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
            postUrl = server.getPostUrl(post.id)
        )
    }
}

@JvmName("danbooruMapToPost")
fun List<DanbooruPost>.mapToPost(server: Server): List<Post> {
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
            postId = post.id,
            serverId = server.serverId,
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
            postUrl = server.getPostUrl(post.id)
        )
    }
}

@JvmName("moebooruMapToPost")
fun List<MoebooruPost>.mapToPost(server: Server): List<Post> {
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
            serverId = server.serverId,
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
            postUrl = server.getPostUrl(post.id)
        )
    }
}

fun GelbooruPostResponse.mapToPost(server: Server): List<Post>? {
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
            serverId = server.serverId,
            change = post.change,
            md5 = post.md5,
            creatorId = post.creatorId,
            hasChildren = post.hasChildren,
            createdAt = post.createdAt?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)),
            status = post.status,
            source = post.source,
            hasNotes = post.hasNotes,
            hasComments = post.hasComments,
            postUrl = server.getPostUrl(post.id)
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