package com.faldez.bonito.data

import android.util.Log
import com.faldez.bonito.model.ServerType
import com.faldez.bonito.model.Tag
import com.faldez.bonito.model.response.GelbooruTagResponse
import com.faldez.bonito.service.Action
import com.faldez.bonito.service.BooruService

class TagRepository(private val service: BooruService) {
    suspend fun queryTags(action: Action.SearchTag): List<Tag>? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                service.gelbooru.getTags(action.buildGelbooruUrl().toString()).mapToTags()
            }
            ServerType.Danbooru -> {
                TODO("not yet implemented")
            }
            null -> {
                null
            }
        }
    }

    suspend fun getTag(action: Action.GetTag): Tag? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                service.gelbooru.getTags(action.buildGelbooruUrl().toString()).mapToTag()
            }
            ServerType.Danbooru -> {
                TODO("not yet implemented")
            }
            null -> {
                null
            }
        }
    }

    suspend fun getTags(action: Action.GetTags): List<Tag>? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                val url = action.buildGelbooruUrl().toString()
                Log.d("TagRepository", url)
                val tags = service.gelbooru.getTags(url).mapToTags()

                action.tags.trim().split(" ").map { name ->
                    tags?.find { it.name == name } ?: Tag(id = 0,
                        name = name,
                        count = 0,
                        type = 0,
                        ambiguous = false)
                }
            }
            ServerType.Danbooru -> {
                TODO("not yet implemented")
            }
            null -> {
                null
            }
        }
    }

    private fun GelbooruTagResponse.mapToTags() =
        this.tags.tag?.map {
            Tag(
                id = it.id,
                name = it.name,
                count = it.count,
                type = it.type,
                ambiguous = it.ambiguous
            )
        }

    private fun GelbooruTagResponse.mapToTag() =
        this.tags.tag?.first()?.let {
            Tag(
                id = it.id,
                name = it.name,
                count = it.count,
                type = it.type,
                ambiguous = it.ambiguous
            )
        }
}
