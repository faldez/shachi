package com.faldez.shachi.service

import com.faldez.shachi.data.model.SavedSearchServer
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.model.ServerView
import com.faldez.shachi.data.repository.PostRepository.Companion.NETWORK_PAGE_SIZE
import okhttp3.HttpUrl

sealed class Action {
    /*
    Search post
     */
    data class SearchPost(
        val server: ServerView,
        val tags: String = "*",
        val limit: Int = NETWORK_PAGE_SIZE
    ) :
        Action() {
        fun buildGelbooruUrl(page: Int): HttpUrl? {
            return server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("user_id", it.username).build()

            }
        }

        fun buildMoebooruUrl(page: Int): HttpUrl? {
            return server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("post.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("password_hash", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }

        fun buildDanbooruUrl(page: Int): HttpUrl? {
            return server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("posts.json")
                    .addQueryParameter("page", page.toString())
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("tags", tags)
                    .addQueryParameter("api_key", it.password)
                    .addQueryParameter("login", it.username).build()
            }
        }
    }

    /*
    Search post of saved search tags
     */
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

        fun buildMoebooruUrl(page: Int, limit: Int = NETWORK_PAGE_SIZE): HttpUrl? {
            return savedSearch.server.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("post.json")
                    .addQueryParameter("page", page.toString())
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

    /*
    Search tag with pattern
     */
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

        fun buildMoebooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tag.json")
                    .addQueryParameter("name", "$tag*")
                    .addQueryParameter("order", "count")
                    .addQueryParameter("limit", limit.toString())
                    .build()
            }
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tags.json")
                    .addQueryParameter("search[name_like]", "$tag*")
                    .addQueryParameter("search[order]", "count")
                    .build()

            }
        }
    }

    /*
    Get single exact tag details
     */
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

        fun buildMoebooruUrl(): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("tag.json")
                    .addQueryParameter("name", "$tag*")
                    .addQueryParameter("limit", "1")
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

    /*
    Get multiple tags details
     */
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

        fun buildMoebooruUrl(): HttpUrl? = null

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

    data class GetTagsSummary(val server: Server?) : Action() {
        fun buildMoebooruUrl(): HttpUrl? = server?.let {
            HttpUrl.get(it.url).newBuilder().addPathSegment("tag").addPathSegment("summary.json")
                .build()
        }
    }

    data class GetComments(val server: Server, val postId: Int) : Action() {
        fun buildGelbooruUrl(): HttpUrl? {
            return HttpUrl.get(server.url).newBuilder().addPathSegment("index.php")
                .addQueryParameter("page", "dapi")
                .addQueryParameter("q", "index").addQueryParameter("s", "comment")
                .addQueryParameter("post_id", postId.toString())
                .build()
        }

        fun buildMoebooruUrl(): HttpUrl? {
            return HttpUrl.get(server.url).newBuilder().addPathSegment("comment.json")
                .addQueryParameter("post_id", postId.toString()).build()
        }

        fun buildDanbooruUrl(): HttpUrl? {
            return HttpUrl.get(server.url).newBuilder().addPathSegment("comments.json")
                .addQueryParameter("group_by", "comment")
                .addQueryParameter("search[post_id]", postId.toString())
                .addQueryParameter("only",
                    "id,post_id,body,score,created_at,updated_at,updater_id,do_not_bump_post,is_deleted,is_sticky,creator[id,name]")
                .build()
        }
    }
}
