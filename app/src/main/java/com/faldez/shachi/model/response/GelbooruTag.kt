package com.faldez.shachi.model.response

import com.faldez.shachi.util.type_adapter.NumberAsBooleanTypeAdapter
import com.faldez.shachi.util.type_adapter.SingleObjectAsArrayTypeAdapter
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type


data class GelbooruTagResponse(
    val tags: GelbooruTags?,
)

data class GelbooruTags(
    @JsonAdapter(SingleObjectAsArrayTypeAdapter::class)
    val tag: List<GelbooruTag>?,
    val type: String,
)

data class GelbooruTag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    @JsonAdapter(NumberAsBooleanTypeAdapter::class)
    val ambiguous: Boolean,
)