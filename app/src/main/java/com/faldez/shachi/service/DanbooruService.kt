package com.faldez.shachi.service

import com.faldez.shachi.model.response.DanbooruPostResponse
import com.faldez.shachi.model.response.DanbooruTagResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


interface DanbooruService {
    @GET
    suspend fun getPosts(@Url url: String): DanbooruPostResponse

    @GET
    suspend fun getTags(@Url url: String): DanbooruTagResponse

    companion object {
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
