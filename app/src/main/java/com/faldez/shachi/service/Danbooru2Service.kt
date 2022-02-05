package com.faldez.shachi.service

import com.faldez.shachi.model.response.Danbooru2Post
import com.faldez.shachi.model.response.Danbooru2Tag
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


interface Danbooru2Service {
    @GET
    suspend fun getPosts(@Url url: String): List<Danbooru2Post>

    @GET
    suspend fun getTags(@Url url: String): List<Danbooru2Tag>

    companion object {
        const val STARTING_PAGE_INDEX = 1

        private val retrofit2Service: Danbooru2Service by lazy {
            val client =
                OkHttpClient().newBuilder().build()
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(Danbooru2Service::class.java)
        }

        fun getInstance(): Danbooru2Service {
            return retrofit2Service
        }
    }
}
