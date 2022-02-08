package com.faldez.shachi.repository

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.Tag
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.model.response.mapToTagDetail
import com.faldez.shachi.model.response.mapToTagDetails
import com.faldez.shachi.model.response.mapToTags
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import kotlinx.coroutines.delay

class TagRepository(private val service: BooruService, private val db: AppDatabase) {
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
                null
            }
        }
    }

    suspend fun getTag(action: Action.GetTag): Tag? {
        val tag = db.tagDao().getTag(action.tag)?.let {
            Tag(name = it.name, type = it.type)
        } ?: when (action.server?.type) {
            ServerType.Gelbooru -> {
                action.buildGelbooruUrl()?.toString()?.let {
                    service.gelbooru.getTags(it).mapToTagDetail()
                }
            }
            ServerType.Danbooru -> {
                action.buildDanbooruUrl()?.toString()?.let {
                    service.danbooru.getTags(it).mapToTagDetail()
                }
            }
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    service.moebooru.getTags(it).mapToTagDetail()
                }
            }
            null -> {
                null
            }
        }?.let {
            Tag(name = it.name, type = it.type)
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
        val tagsToQuery = action.tags.trim().split(" ")
        val cachedTags = tagsToQuery.let {
            val where = it.joinToString(" OR ") {
                "name = ?"
            }
            Pair(it, where)
        }.let { (tags, where) ->
            val queryStr = "SELECT * FROM tag WHERE $where"
            Log.d("TagRepository/getTags", "queryStr $queryStr")
            val sqlQuery = SimpleSQLiteQuery(queryStr, tags.toTypedArray()
            )
            val result = db.tagDao().getTags(sqlQuery)
            if (result.isNullOrEmpty()) null else result
        }
        Log.d("TagRepository/getTags", "cachedTags $cachedTags")

        // filter tags from tagsToQuery that is not on database to query to server
        val cachedTagsSet = cachedTags?.map { it.name }?.toSet()
        val uncachedTags = tagsToQuery.filter { cachedTagsSet?.contains(it) != true }
        Log.d("TagRepository/getTags", "uncachedTags $uncachedTags")

        if (uncachedTags.isNullOrEmpty())
            return cachedTags

        // create new action which contains tags not found on database
        val newAction = action.copy(tags = uncachedTags.joinToString(" "))

        val bulkQueriedTags = when (action.server?.type) {
            ServerType.Gelbooru -> {
                newAction.buildGelbooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Gelbooru", url)
                    service.gelbooru.getTags(url).mapToTags()
                }
            }
            ServerType.Danbooru -> {
                newAction.buildDanbooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Danbooru", url)
                    service.danbooru.getTags(url).mapToTags()
                }

            }
            ServerType.Moebooru -> {
                newAction.buildMoebooruUrl()?.toString()?.let { url ->
                    Log.d("TagRepository/Moebooru", url)
                    service.moebooru.getTags(url).mapToTags()
                }
            }
            null -> {
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
            uncachedTags.filter { bulkQueriedTagMap?.containsKey(it) != true }.mapNotNull {
                Log.d("TagRepository/getTags", "eachQueriedTags $it")
                delay(100)
                val eachTagAction = Action.GetTag(server = action.server, it)
                getTag(eachTagAction)
            }

        val result =
            (listOf(cachedTags ?: listOf(), bulkQueriedTags ?: listOf(),
                eachQueriedTags).flatten() as List<*>).filterIsInstance<Tag>()

        Log.d("TagRepository/getTags", "result $result")

        return result

    }

    suspend fun getTagsSummary(action: Action.GetTagsSummary) {
        when (action.server?.type) {
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    service.moebooru.getTagsSummary(it).data.split(" ").mapNotNull { summary ->
                        try {
                            summary.trim().split("`").let { split ->
                                val type = split[0].toInt()
                                split.subList(1, split.size).mapNotNull { tag ->
                                    if (tag.isNotEmpty()) Tag(name = tag, type = type)
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
        }.also {
            if (it != null) db.tagDao().insertTags(it)
        }
    }
}
