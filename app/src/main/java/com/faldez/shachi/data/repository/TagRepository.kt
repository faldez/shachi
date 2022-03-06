package com.faldez.shachi.data.repository

import android.util.Log
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.model.response.mapToTag
import com.faldez.shachi.data.model.response.mapToTagDetails
import com.faldez.shachi.data.model.response.mapToTags
import com.faldez.shachi.data.model.response.parseTag
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import kotlinx.coroutines.delay

class TagRepository(private val service: BooruService, private val db: AppDatabase) {
    suspend fun insertTags(tags: List<Tag>) {
        db.tagDao().insertTags(tags)
    }

    suspend fun queryTags(action: Action.SearchTag): List<TagDetail>? {
        return when (action.server?.type) {
            ServerType.Gelbooru -> {
                action.buildGelbooruUrl()?.toString()?.let {
                    Log.d("TagRepository/Gelbooru", it)
                    service.gelbooru.getTags(it).mapToTagDetails()
                }
            }
            ServerType.Danbooru -> {
                action.buildDanbooruUrl()?.toString()?.let {
                    Log.d("TagRepository/Danbooru", it)
                    service.danbooru.getTags(it).mapToTagDetails()
                }
            }
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    Log.d("TagRepository/Moebooru", it)
                    service.moebooru.getTags(it).mapToTagDetails()
                }
            }
            null -> {
                db.tagDao().searchTag("${action.tag}%")?.mapToTagDetails()
            }
        }
    }

    suspend fun getTag(action: Action.GetTag): Tag? {
        val tag = if (action.server != null) {
            db.tagDao().getTag(action.server.serverId, action.tag)
        } else {
            db.tagDao().getTag(action.tag)
        }
            ?: when (action.server?.type) {
                ServerType.Gelbooru -> {
                    action.buildGelbooruUrl()?.toString()?.let {
                        service.gelbooru.getTags(it).mapToTag(action.server.serverId)
                    }
                }
                ServerType.Danbooru -> {
                    action.buildDanbooruUrl()?.toString()?.let {
                        service.danbooru.getTags(it).mapToTag(action.server.serverId)
                    }
                }
                ServerType.Moebooru -> {
                    action.buildMoebooruUrl()?.toString()?.let {
                        service.moebooru.getTags(it).mapToTag(action.server.serverId)
                    }
                }
                else -> null
            }?.also {
                db.tagDao().insertTag(it)
            }

        return tag
    }

    suspend fun getTags(action: Action.GetTags): List<Tag>? {
        Log.d("TagRepository/getTags", "tags ${action.tags}")
        /*
        Try to query from database first before make request
         */
        val modifierPrefixRegex = Regex(modifierRegex)
        val tagsToQuery = action.tags.trim().split(" ")
        val cachedTags = tagsToQuery.let { tags ->
            val cleanedTagsToQuery = tags.map { it.replaceFirst(modifierPrefixRegex, "") }
            val result = if (action.server != null) {
                db.tagDao().getTags(action.server.serverId, cleanedTagsToQuery)
            } else {
                db.tagDao().getTags(cleanedTagsToQuery)
            }?.associateBy { it.name }
            cleanedTagsToQuery.mapIndexedNotNull { index, tag ->
                result?.get(tag)?.copy(name = tagsToQuery[index])
            }
        }

        Log.d("TagRepository/getTags", "cachedTags $cachedTags")

        // filter tags from tagsToQuery that is not on database to query to server
        val cachedTagsSet = cachedTags.map { it.name }.toSet()
        val uncachedTags = tagsToQuery.filter {
            !cachedTagsSet.contains(it.replaceFirst(modifierPrefixRegex,
                ""))
        }
        Log.d("TagRepository/getTags", "uncachedTags $uncachedTags")

        if (uncachedTags.isNullOrEmpty())
            return cachedTags

        // create new action which contains tags not found on database
        val newAction = action.copy(tags = uncachedTags.joinToString(" "))

        val bulkQueriedTags = when (action.server?.type) {
            ServerType.Gelbooru -> {
                newAction.buildGelbooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Gelbooru", url)
                    service.gelbooru.getTags(url).mapToTags(action.server.serverId)
                }
            }
            ServerType.Danbooru -> {
                newAction.buildDanbooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Danbooru", url)
                    service.danbooru.getTags(url).mapToTags(action.server.serverId)
                }

            }
            ServerType.Moebooru -> {
                Log.d("TagRepository/Moebooru", "Moebooru can't get multiple tags at once")
                null
            }
            else -> {
                null
            }
        }.let { tags ->
            // only get tags specified by action, create new generic tag if not found on remote server
            tags?.associateBy { it.name }?.let { tagsMap ->
                uncachedTags.mapNotNull { name ->
                    tagsMap[name]
                }
            }
        }

        if (bulkQueriedTags?.isNotEmpty() == true) {
            db.tagDao().insertTags(bulkQueriedTags)
        }

        val bulkQueriedTagMap = bulkQueriedTags?.associateBy { it.name }

        val eachQueriedTags =
            uncachedTags.filter { bulkQueriedTagMap?.containsKey(it) != true }.mapNotNull { name ->
                Log.d("TagRepository/getTags", "eachQueriedTags $name")
                delay(100)
                action.server?.let { server ->
                    val eachTagAction = Action.GetTag(server = server, name)
                    getTag(eachTagAction)
                }
            }

        val result =
            (listOf(cachedTags, bulkQueriedTags ?: listOf(),
                eachQueriedTags).flatten() as List<*>).filterIsInstance<Tag>()

        Log.d("TagRepository/getTags", "result $result")

        return result
    }

    suspend fun getTagsSummary(action: Action.GetTagsSummary): List<Tag>? {
        return when (action.server?.type) {
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    service.moebooru.getTagsSummary(it).data.split(" ").mapNotNull { summary ->
                        try {
                            summary.trim().split("`").let { split ->
                                val type = split[0].toInt()
                                split.subList(1, split.size).mapNotNull { tag ->
                                    if (tag.isNotEmpty()) Tag(name = tag,
                                        type = parseTag(type),
                                        serverId = action.server.serverId)
                                    else null
                                }
                            }
                        } catch (exception: Exception) {
                            null
                        }
                    }.flatten()
                }
            }
            else -> null
        }
    }
}
