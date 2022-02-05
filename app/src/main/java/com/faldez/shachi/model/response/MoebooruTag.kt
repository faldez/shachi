package com.faldez.shachi.model.response

import com.faldez.shachi.util.type_adapter.NumberAsBooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter

data class MoebooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean,
)
