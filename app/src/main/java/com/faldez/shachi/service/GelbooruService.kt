package com.faldez.shachi.service

import com.faldez.shachi.model.response.GelbooruPostResponse
import com.faldez.shachi.model.response.GelbooruTagResponse
import com.faldez.shachi.util.interceptor.XmlToJsonInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


interface GelbooruService {
    @GET
    suspend fun getPosts(@Url url: String): GelbooruPostResponse

    @GET
    suspend fun getTags(@Url url: String): GelbooruTagResponse

    companion object {
        private val retrofitService: GelbooruService by lazy {
            val client =
                OkHttpClient().newBuilder().addInterceptor(XmlToJsonInterceptor()).build()
            Retrofit.Builder().client(client)
                .baseUrl("https://safebooru.org")
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(GelbooruService::class.java)
        }

        fun getInstance(): GelbooruService {
            return retrofitService
        }
    }
}