package com.faldez.bonito.model

enum class ServerType {
    Gelbooru,
    Danbooru
}

data class Server(
    val type: ServerType,
    val title: String,
    val url: String,
)
