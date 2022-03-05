package com.faldez.shachi.data.model.response

data class MoebooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean,
)

data class MoebooruTagSummary(
    val version: Int,
    val data: String,
)