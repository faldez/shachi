package com.faldez.bonito.service

import com.faldez.bonito.model.Server
import okhttp3.HttpUrl

sealed class Action {
    data class SearchPost(val server: Server?, val tags: String) : Action() {
        fun buildGelbooruUrl(page: Int): HttpUrl? {
            return server?.let {
                HttpUrl.get(it.url).newBuilder().addPathSegment("index.php")
                    .addQueryParameter("page", "dapi")
                    .addQueryParameter("q", "index").addQueryParameter("s", "post")
                    .addQueryParameter("pid", page.toString())
                    .addQueryParameter("tags", tags).build()
            }
        }
    }
}
