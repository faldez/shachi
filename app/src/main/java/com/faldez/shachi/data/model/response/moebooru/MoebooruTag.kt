package com.faldez.shachi.data.model.response.moebooru

import kotlinx.serialization.Serializable

@Serializable
data class MoebooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean,
)

@Serializable
data class MoebooruTagSummary(
    val version: Int,
    val data: String,
)