package com.faldez.shachi.data.api

import com.faldez.shachi.data.model.response.danbooru.DanbooruComment
import com.faldez.shachi.data.model.response.danbooru.DanbooruPost
import com.faldez.shachi.data.model.response.danbooru.DanbooruTag
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit


interface DanbooruApi {
    @GET
    suspend fun getPosts(@Url url: String): List<DanbooruPost>

    @GET
    suspend fun getTags(@Url url: String): List<DanbooruTag>

    @GET
    suspend fun getComments(@Url url: String): List<DanbooruComment>

    companion object {
        const val STARTING_PAGE_INDEX = 1

        private val retrofitApi: DanbooruApi by lazy {
            val client =
                OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build()
            val contentType = "application/json".toMediaType()
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(json.asConverterFactory(contentType)).build()
                .create(DanbooruApi::class.java)
        }

        fun getInstance(): DanbooruApi {
            return retrofitApi
        }
    }
}
