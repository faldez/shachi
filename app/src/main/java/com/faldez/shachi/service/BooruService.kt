package com.faldez.shachi.service

class BooruService {
    val gelbooru: GelbooruService by lazy {
        GelbooruService.getInstance()
    }
    val danbooru: DanbooruService by lazy {
        DanbooruService.getInstance()
    }
}