package com.faldez.shachi.data.api

import com.faldez.shachi.data.model.response.MoebooruComment
import com.faldez.shachi.data.model.response.MoebooruPost
import com.faldez.shachi.data.model.response.MoebooruTag
import com.faldez.shachi.data.model.response.MoebooruTagSummary
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url


interface MoebooruApi {
    @GET
    suspend fun getPosts(@Url url: String): List<MoebooruPost>

    @GET
    suspend fun getTags(@Url url: String): List<MoebooruTag>

    @GET
    suspend fun getTagsSummary(@Url url: String): MoebooruTagSummary

    @GET
    suspend fun getComments(@Url url: String): List<MoebooruComment>

    companion object {
        const val STARTING_PAGE_INDEX = 1

        private val retrofit2Api: MoebooruApi by lazy {
            val client =
                OkHttpClient().newBuilder().build()
            val contentType = "application/json".toMediaType()
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(Json.asConverterFactory(contentType)).build()
                .create(MoebooruApi::class.java)
        }

        fun getInstance(): MoebooruApi {
            return retrofit2Api
        }
    }
}
