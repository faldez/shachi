package com.faldez.shachi.data.repository.tag

import android.util.Log
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.model.response.mapToTag
import com.faldez.shachi.data.model.response.mapToTagDetails
import com.faldez.shachi.data.model.response.mapToTags
import com.faldez.shachi.data.model.response.parseTag
import com.faldez.shachi.data.api.Action
import com.faldez.shachi.data.api.BooruApi
import kotlinx.coroutines.delay

interface TagRepository {
    val booruApi: BooruApi
    val db: AppDatabase

    suspend fun insertTags(tags: List<Tag>) = db.tagDao().insertTags(tags)


    suspend fun queryTags(action: Action.SearchTag): List<TagDetail>? = when (action.server?.type) {
        ServerType.Gelbooru -> {
            action.buildGelbooruUrl()?.toString()?.let {
                Log.d("TagRepository/Gelbooru", it)
                booruApi.gelbooru.getTags(it).mapToTagDetails()
            }
        }
        ServerType.Danbooru -> {
            action.buildDanbooruUrl()?.toString()?.let {
                Log.d("TagRepository/Danbooru", it)
                booruApi.danbooru.getTags(it).mapToTagDetails()
            }
        }
        ServerType.Moebooru -> {
            action.buildMoebooruUrl()?.toString()?.let {
                Log.d("TagRepository/Moebooru", it)
                booruApi.moebooru.getTags(it).mapToTagDetails()
            }
        }
        null -> {
            db.tagDao().searchTag("${action.tag}%")?.mapToTagDetails()
        }
    }


    suspend fun getTag(action: Action.GetTag): Tag? = if (action.server != null) {
        db.tagDao().getTag(action.server.serverId, action.tag)
    } else {
        db.tagDao().getTag(action.tag)
    }
        ?: when (action.server?.type) {
            ServerType.Gelbooru -> {
                action.buildGelbooruUrl()?.toString()?.let {
                    booruApi.gelbooru.getTags(it).mapToTag(action.server.serverId)
                }
            }
            ServerType.Danbooru -> {
                action.buildDanbooruUrl()?.toString()?.let {
                    booruApi.danbooru.getTags(it).mapToTag(action.server.serverId)
                }
            }
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    booruApi.moebooru.getTags(it).mapToTag(action.server.serverId)
                }
            }
            else -> null
        }?.also {
            db.tagDao().insertTag(it)
        }

    suspend fun getTags(action: Action.GetTags): List<Tag>? {
        Log.d("TagRepository/getTags", "tags ${action.tags}")
        /*
        Try to query from database first before make request
         */
        val modifierPrefixRegex = Regex(modifierRegex)
        val tagsToQuery = action.tags.trim().split(" ")
        val cachedTags = tagsToQuery.let { tags ->
            if (action.server != null) {
                db.tagDao().getTags(action.server.serverId, tags)
            } else {
                db.tagDao().getTags(tags)
            }
        }

        Log.d("TagRepository/getTags", "cachedTags $cachedTags")

        // filter tags from tagsToQuery that is not on database to query to server
        val cachedTagsSet = cachedTags?.map { it.name }?.toSet()
        val uncachedTags = tagsToQuery.filter {
            !(cachedTagsSet?.contains(it) ?: false)
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
                    booruApi.gelbooru.getTags(url).mapToTags(action.server.serverId)
                }
            }
            ServerType.Danbooru -> {
                newAction.buildDanbooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Danbooru", url)
                    booruApi.danbooru.getTags(url).mapToTags(action.server.serverId)
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
            (listOf(cachedTags ?: listOf(), bulkQueriedTags ?: listOf(),
                eachQueriedTags).flatten() as List<*>).filterIsInstance<Tag>()

        Log.d("TagRepository/getTags", "result $result")

        return result
    }

    suspend fun getTagsSummary(action: Action.GetTagsSummary): List<Tag>? =
        when (action.server?.type) {
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    booruApi.moebooru.getTagsSummary(it).data.split(" ").mapNotNull { summary ->
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
