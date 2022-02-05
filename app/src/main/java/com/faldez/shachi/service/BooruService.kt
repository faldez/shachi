package com.faldez.shachi.service

class BooruService {
    val gelbooru: GelbooruService by lazy {
        GelbooruService.getInstance()
    }
    val danbooru2: Danbooru2Service by lazy {
        Danbooru2Service.getInstance()
    }
}