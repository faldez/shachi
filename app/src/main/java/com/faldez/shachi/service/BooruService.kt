package com.faldez.shachi.service

class BooruService {
    val gelbooru: GelbooruService by lazy {
        GelbooruService.getInstance()
    }
    val moebooru: MoebooruService by lazy {
        MoebooruService.getInstance()
    }
    val danbooru: DanbooruService by lazy {
        DanbooruService.getInstance()
    }
}