package com.faldez.shachi.util.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.jsonjava.XML


class XmlToJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        var body = response.body
        if (body?.contentType()?.subtype == "xml") {
            val json = XML.toJSONObject(body.string())
            Log.d("TransformInterceptor", json.toString())
            body = ResponseBody.create("application/json".toMediaTypeOrNull(), json.toString())
            val builder = response.newBuilder()
            return builder.header("Content-Type", "application/json").headers(response.headers)
                .body(body).build()
        }

        Log.d("TransformInterceptor", "pass trough")
        return response
    }

}