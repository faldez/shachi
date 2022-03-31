package com.faldez.shachi.data.api

import com.faldez.shachi.data.model.response.GelbooruCommentResponse
import com.faldez.shachi.data.model.response.GelbooruPostResponse
import com.faldez.shachi.data.model.response.GelbooruTagResponse
import com.faldez.shachi.data.util.interceptor.XmlToJsonInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit


interface GelbooruApi {
    @GET
    suspend fun getPosts(@Url url: String): GelbooruPostResponse

    @GET
    suspend fun getTags(@Url url: String): GelbooruTagResponse

    @GET
    suspend fun getComments(@Url url: String): GelbooruCommentResponse

    companion object {
        const val STARTING_PAGE_INDEX = 0

        private val retrofitApi: GelbooruApi by lazy {
            val client =
                OkHttpClient().newBuilder().addInterceptor(XmlToJsonInterceptor())
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
                .create(GelbooruApi::class.java)
        }

        fun getInstance(): GelbooruApi {
            return retrofitApi
        }
    }
}