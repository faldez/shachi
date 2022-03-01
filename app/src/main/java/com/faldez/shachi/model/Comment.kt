package com.faldez.shachi.model

import java.time.LocalDateTime

data class Comment(
    val id: Int,
    val postId: Int,
    val body: String,
    val creator: String,
    val creatorId: Int?,
    val createdAt: LocalDateTime?,
)
