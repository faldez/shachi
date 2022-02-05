package com.faldez.shachi.repository

import android.util.Log
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.Tag
import com.faldez.shachi.model.response.Danbooru2Tag
import com.faldez.shachi.model.response.GelbooruTagResponse
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService

class TagRepository(private val service: BooruService) {
    suspend fun queryTags(action: Action.SearchTag): List<Tag>? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                service.gelbooru.getTags(action.buildGelbooruUrl().toString()).mapToTags()
            }
            ServerType.Danbooru -> {
                service.danbooru2.getTags(action.buildDanbooruUrl().toString()).mapToTags()
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
                service.danbooru2.getTags(action.buildDanbooruUrl().toString()).mapToTag()
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
                val url = action.buildDanbooruUrl().toString()
                Log.d("TagRepository", url)
                val tags = service.danbooru2.getTags(url).mapToTags()

                action.tags.trim().split(" ").map { name ->
                    tags?.find { it.name == name } ?: Tag(id = 0,
                        name = name,
                        count = 0,
                        type = 0,
                        ambiguous = false)
                }
            }
            null -> {
                null
            }
        }
    }

    private fun GelbooruTagResponse.mapToTags() =
        this.tags?.tag?.map {
            Tag(
                id = it.id,
                name = it.name,
                count = it.count,
                type = it.type,
                ambiguous = it.ambiguous
            )
        }

    private fun GelbooruTagResponse.mapToTag() =
        this.tags?.tag?.first()?.let {
            Tag(
                id = it.id,
                name = it.name,
                count = it.count,
                type = it.type,
                ambiguous = it.ambiguous
            )
        }

    private fun List<Danbooru2Tag>.mapToTags() =
        this.map {
            Tag(
                id = it.id,
                name = it.name,
                count = it.postCount,
                type = it.category,
                ambiguous = false
            )
        }

    private fun List<Danbooru2Tag>.mapToTag() =
        this.first().let {
            Tag(
                id = it.id,
                name = it.name,
                count = it.postCount,
                type = it.category,
                ambiguous = false
            )
        }
}
