package com.faldez.shachi.service

import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.repository.PostRepository.Companion.NETWORK_PAGE_SIZE
import okhttp3.HttpUrl

sealed class Action {
    data class SearchPost(
        val server: ServerView?,
        val tags: String = "*",
    ) :
        Action() {
        fun buildGelbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags).build()
            }
        }

        fun buildDanbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("posts.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags).build()
            }
        }
    }

    data class SearchSavedSearchPost(val savedSearch: SavedSearchServer) : Action() {
        fun buildGelbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", savedSearch.savedSearch.tags).build()
            }
        }

        fun buildDanbooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("posts.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", savedSearch.savedSearch.tags).build()
            }
        }
    }

    data class SearchTag(val server: Server?, val tag: String, val limit: Int = 10) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "tag")
                    .addQueryParameter("name_pattern", "$tag%")
                    .addQueryParameter("order", "DESC")
                    .addQueryParameter("orderby", "count")
                    .addQueryParameter("limit", limit.toString())
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tags.json")
                    .addQueryParameter("search[name_like]", "$tag*").build()

            }
        }
    }

    data class GetTag(val server: Server?, val tag: String) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "tag")
                    .addQueryParameter("name", tag)
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tags.json")
                    .addQueryParameter("search[name]", tag)
                    .build()
            }
        }
    }

    data class GetTags(val server: Server?, val tags: String) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "tag")
                    .addQueryParameter("names", tags)
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tags.json").apply {
                    tags.split(" ").forEach { tag ->
                        addQueryParameter("search[name_array][]", tag)
                    }
                }.build()

            }
        }
    }
}
