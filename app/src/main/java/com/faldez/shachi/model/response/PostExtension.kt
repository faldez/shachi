package com.faldez.shachi.model.response

import android.util.Log
import androidx.core.os.LocaleListCompat
import com.faldez.shachi.model.Post
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
            rating = post.rating,
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
    return this.map { post ->
        Log.d("mapToPost", "$post")
        Post(
            height = post.imageHeight,
            width = post.imageWidth,
            score = post.score,
            fileUrl = post.largeFileUrl ?: "",
            parentId = post.parentId,
            sampleUrl = post.fileUrl,
            sampleWidth = null,
            sampleHeight = null,
            previewUrl = post.previewFileUrl,
            previewWidth = null,
            previewHeight = null,
            rating = post.rating,
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
            rating = post.rating,
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