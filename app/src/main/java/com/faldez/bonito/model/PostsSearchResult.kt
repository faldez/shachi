package com.faldez.bonito.model

import java.lang.Exception

sealed class PostsSearchResult {
    data class Success(val data: List<Post>) : PostsSearchResult()
    data class Error(val error: Exception) : PostsSearchResult()
}