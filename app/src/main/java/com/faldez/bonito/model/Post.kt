package com.faldez.bonito.model

import android.os.Parcelable
import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type
import java.time.format.DateTimeFormatter
import com.google.gson.JsonPrimitive

import com.google.gson.JsonParseException

import com.google.gson.JsonDeserializationContext

import com.google.gson.JsonElement

import com.google.gson.JsonDeserializer
import com.google.gson.annotations.JsonAdapter
import java.time.ZonedDateTime

data class Posts(
    val post: List<Post>?,
    val count: Int,
    val offset: Int,
)

@Parcelize
data class Post(
    val height: Int,
    val width: Int,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) val score: Int?,
    @SerializedName("file_url") val fileUrl: String,
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class) @SerializedName("parent_id") val parentId: Int?,
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
    @JsonAdapter(ZonedDateTimeAdapter::class) @SerializedName("created_at") val createdAt: ZonedDateTime?,
    val status: String,
    val source: String,
    @SerializedName("has_notes") val hasNotes: Boolean,
    @SerializedName("has_comments") val hasComments: Boolean,
) : Parcelable

internal class ZonedDateTimeAdapter : JsonDeserializer<ZonedDateTime?>,
    JsonSerializer<ZonedDateTime?> {
    val format = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss Z yyyy")

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ZonedDateTime? {
        return ZonedDateTime.parse(json?.asString, format)
    }

    override fun serialize(
        src: ZonedDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(src?.format(format)!!)
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
        } else if (jsonElement.isJsonObject) {
            return null
        }
        return context.deserialize(jsonElement, type)
    }
}