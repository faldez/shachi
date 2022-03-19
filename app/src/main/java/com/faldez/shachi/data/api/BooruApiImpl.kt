package com.faldez.shachi.data.api

class BooruApiImpl : BooruApi {
    override val gelbooru: GelbooruApi by lazy {
        GelbooruApi.getInstance()
    }
    override val moebooru: MoebooruApi by lazy {
        MoebooruApi.getInstance()
    }
    override val danbooru: DanbooruApi by lazy {
        DanbooruApi.getInstance()
    }
}