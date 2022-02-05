package com.faldez.shachi.repository

import android.util.Log
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.Tag
import com.faldez.shachi.model.response.mapToTag
import com.faldez.shachi.model.response.mapToTags
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService

class TagRepository(private val service: BooruService) {
    suspend fun queryTags(action: Action.SearchTag): List<Tag>? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                service.gelbooru.getTags(action.buildGelbooruUrl().toString()).mapToTags()
            }
            ServerType.Danbooru -> {
                service.danbooru.getTags(action.buildDanbooruUrl().toString()).mapToTags()
            }
            ServerType.Moebooru -> {
                service.moebooru.getTags(action.buildMoebooruUrl().toString()).mapToTags()
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
                service.danbooru.getTags(action.buildDanbooruUrl().toString()).mapToTag()
            }
            ServerType.Moebooru -> {
                service.moebooru.getTags(action.buildMoebooruUrl().toString()).mapToTag()
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
                val tags = service.danbooru.getTags(url).mapToTags()

                action.tags.trim().split(" ").map { name ->
                    tags.find { it.name == name } ?: Tag(id = 0,
                        name = name,
                        count = 0,
                        type = 0,
                        ambiguous = false)
                }
            }
            ServerType.Moebooru -> {
                val tags = action.buildMoebooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository", url)
                    service.moebooru.getTags(url).mapToTags()
                }

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
}
