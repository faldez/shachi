package com.faldez.shachi.service

abstract class BooruService {
    abstract val gelbooru: GelbooruService
    abstract val moebooru: MoebooruService
    abstract val danbooru: DanbooruService
}

class BooruServiceImpl : BooruService() {
    override val gelbooru: GelbooruService by lazy {
        GelbooruService.getInstance()
    }
    override val moebooru: MoebooruService by lazy {
        MoebooruService.getInstance()
    }
    override val danbooru: DanbooruService by lazy {
        DanbooruService.getInstance()
    }
}