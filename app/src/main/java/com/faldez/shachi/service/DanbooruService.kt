package com.faldez.shachi.service

import com.faldez.shachi.data.model.response.DanbooruComment
import com.faldez.shachi.data.model.response.DanbooruPost
import com.faldez.shachi.data.model.response.DanbooruTag
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


interface DanbooruService {
    @GET
    suspend fun getPosts(@Url url: String): List<DanbooruPost>

    @GET
    suspend fun getTags(@Url url: String): List<DanbooruTag>

    @GET
    suspend fun getComments(@Url url: String): List<DanbooruComment>

    companion object {
        const val STARTING_PAGE_INDEX = 1

        private val retrofitService: DanbooruService by lazy {
            val client =
                OkHttpClient().newBuilder().build()
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(DanbooruService::class.java)
        }

        fun getInstance(): DanbooruService {
            return retrofitService
        }
    }
}
