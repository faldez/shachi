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
