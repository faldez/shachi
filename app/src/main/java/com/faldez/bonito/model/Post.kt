package com.faldez.bonito.model

import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.gson.JsonPrimitive

import com.google.gson.JsonParseException

import com.google.gson.JsonDeserializationContext

import com.google.gson.JsonElement

import com.google.gson.JsonDeserializer
import com.google.gson.annotations.JsonAdapter

data class Posts(
    val post: List<Post>?,
    val count: Int,
    val offset: Int,
)

data class Post(
    val height: Int,
    val width: Int,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) val score: Int?,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("parent_id") @JsonAdapter(EmptyStringAsNullTypeAdapter::class)  val parentId: Int?,
    @SerializedName("sample_url") val sampleUrl: String?,
    @SerializedName("sample_width") val sampleWidth: Int?,
    @SerializedName("sample_height") val sampleHeight: Int?,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("preview_width") val previewWidth: Int?,
    @SerializedName("preview_height") val previewHeight: Int?,
    val rating: String,
    val tags: String,
    val id: Int,
    val change: Int,
    val md5: String,
    @SerializedName("creator_id") val creatorId: Int?,
    @SerializedName("has_children") val hasChildren: Boolean,
    @JsonAdapter(LocalDateTimeAdapter::class) @SerializedName("created_at") val createdAt: LocalDateTime?,
    val status: String,
    val source: String,
    @SerializedName("has_notes") val hasNotes: Boolean,
    @SerializedName("has_comments") val hasComments: Boolean,
)

internal class LocalDateTimeAdapter : JsonDeserializer<LocalDateTime?> {    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): LocalDateTime? {
        return LocalDateTime.parse(json?.asString, DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss Z yyyy"))
    }
}

class EmptyStringAsNullTypeAdapter<T>  // Let Gson instantiate it itself
private constructor() : JsonDeserializer<T?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): T? {
        if (jsonElement.isJsonPrimitive) {
            val jsonPrimitive = jsonElement.asJsonPrimitive
            if (jsonPrimitive.isString && jsonPrimitive.asString.isEmpty()) {
                return null
            }
        }
        return context.deserialize(jsonElement, type)
    }
}