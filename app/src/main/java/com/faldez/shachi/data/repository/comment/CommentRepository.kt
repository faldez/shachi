package com.faldez.shachi.data.repository.comment

import android.util.Log
import com.faldez.shachi.data.model.Comment
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.response.mapToComments
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService

interface CommentRepository {
    val service: BooruService

    suspend fun getComments(action: Action.GetComments): List<Comment>? =
        when (action.server.type) {
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