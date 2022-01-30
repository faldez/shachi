package com.faldez.sachi.service

import android.util.Log
import retrofit2.Retrofit
import retrofit2.http.GET
import com.faldez.sachi.model.response.GelbooruPostResponse
import com.faldez.sachi.model.response.GelbooruTagResponse
import okhttp3.*
import org.json.jsonjava.XML

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Url


interface GelbooruService {
    @GET
    suspend fun getPosts(@Url url: String): GelbooruPostResponse

    @GET
    suspend fun getTags(@Url url: String): GelbooruTagResponse

    companion object {
        var retrofitService: GelbooruService? = null

        fun getInstance(): GelbooruService {
            if (retrofitService == null) {
                val client =
                    OkHttpClient().newBuilder().addInterceptor(TransformInterceptor()).build()
                return Retrofit.Builder().client(client)
                    .baseUrl("https://safebooru.org")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                    .create(GelbooruService::class.java)
            }
            return retrofitService!!
        }
    }
}

class TransformInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        var body = response.body()
        if (body?.contentType()?.subtype() == "xml") {
            val json = XML.toJSONObject(body.string())
            Log.d("TransformInterceptor", json.toString())
            body = ResponseBody.create(MediaType.parse("application/json"), json.toString())
            val builder = response.newBuilder()
            return builder.header("Content-Type", "application/json").headers(response.headers())
                .body(body).build()
        }

        Log.d("TransformInterceptor", "pass trough")
        return response
    }

}