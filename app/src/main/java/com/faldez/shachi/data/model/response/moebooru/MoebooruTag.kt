package com.faldez.shachi.data.model.response.moebooru

import kotlinx.serialization.Serializable

@Serializable
data class MoebooruTag(
    val id: Int = 0,
    val name: String = "",
    val count: Int = 0,
    val type: Int = 0,
    val ambiguous: Boolean = false,
)

@Serializable
data class MoebooruTagSummary(
    val version: Int,
    val data: String,
)