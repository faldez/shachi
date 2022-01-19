package com.faldez.bonito.service

import android.util.Log
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import android.util.Xml
import com.faldez.bonito.model.LocalDateTimeAdapter
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.jsonjava.XML

import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDateTime


interface GelbooruService {
    @GET("/index.php?page=dapi&s=post&q=index&limit=100")
    suspend fun getPosts(@Query("pid") page: Int, @Query("tags") tags: String): PostsResponse

//    @GET("/index.php?page=dapi&s=tag&q=index")
//    fun getTags(@Query("name_pattern")pattern: String): ResponseBody

    companion object {
        var retrofitService: GelbooruService? = null

        fun getInstance(baseUrl: String): GelbooruService {
            if (retrofitService == null) {
                val client =
                    OkHttpClient().newBuilder().addInterceptor(TransformInterceptor()).build()
                return Retrofit.Builder().client(client).baseUrl(baseUrl)
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

        return response
    }

}