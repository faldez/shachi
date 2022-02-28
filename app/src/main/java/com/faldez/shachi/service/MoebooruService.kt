package com.faldez.shachi.service

import com.faldez.shachi.model.response.MoebooruPost
import com.faldez.shachi.model.response.MoebooruTag
import com.faldez.shachi.model.response.MoebooruTagSummary
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


interface MoebooruService {
    @GET
    suspend fun getPosts(@Url url: String): List<MoebooruPost>

    @GET
    suspend fun getTags(@Url url: String): List<MoebooruTag>

    @GET
    suspend fun getTagsSummary(@Url url: String): MoebooruTagSummary

    companion object {
        const val STARTING_PAGE_INDEX = 0

        private val retrofit2Service: MoebooruService by lazy {
            val client =
                OkHttpClient().newBuilder().build()
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(MoebooruService::class.java)
        }

        fun getInstance(): MoebooruService {
            return retrofit2Service
        }
    }
}
