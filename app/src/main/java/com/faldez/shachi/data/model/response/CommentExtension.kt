package com.faldez.shachi.data.model.response

import com.faldez.shachi.data.model.Comment

fun GelbooruCommentResponse.mapToComments() = this.comments?.comment?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator,
        creatorId = it.creatorId,
        createdAt = it.createdAt
    )
}

@JvmName("moebooruMapToComment")
fun List<MoebooruComment>?.mapToComments() = this?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator,
        creatorId = it.creatorId,
        createdAt = it.createdAt?.toLocalDateTime()
    )
}

@JvmName("danbooruMapToComment")
fun List<DanbooruComment>?.mapToComments() = this?.map {
    Comment(id = it.id,
        postId = it.postId,
        body = it.body,
        creator = it.creator.name,
        creatorId = it.creator.id,
        createdAt = it.createdAt?.toLocalDateTime()
    )
}