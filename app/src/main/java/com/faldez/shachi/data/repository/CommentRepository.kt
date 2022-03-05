package com.faldez.shachi.data.repository

import android.util.Log
import com.faldez.shachi.data.model.Comment
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.response.mapToComments
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService

class CommentRepository(private val service: BooruService) {
    suspend fun getComments(action: Action.GetComments): List<Comment>? {
        Log.d("CommentRepository", "getComments")
        return when (action.server.type) {
            ServerType.Gelbooru -> {
                action.buildGelbooruUrl()?.toString()?.let {
                    Log.d("CommentRepository/Gelbooru", it)
                    service.gelbooru.getComments(it).mapToComments()
                }
            }
            ServerType.Moebooru -> {
                action.buildMoebooruUrl()?.toString()?.let {
                    Log.d("CommentRepository/Moebooru", it)
                    service.moebooru.getComments(it).mapToComments()
                }
            }
            ServerType.Danbooru -> {
                action.buildDanbooruUrl()?.toString()?.let {
                    Log.d("CommentRepository/Danbooru", it)
                    service.danbooru.getComments(it).mapToComments()
                }
            }
        }
    }
}