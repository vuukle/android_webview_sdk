package com.vuukle.webview.manager.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class BaseApiClient<T>(
    private val classT: Class<T>
) {

    companion object {
        const val CONNECTION_TIMEOUT: Long = 180L
        const val READ_TIMEOUT: Long = 180L
        const val WRITE_TIMEOUT: Long = 180L
    }

    private val loggingInterceptor by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        loggingInterceptor
    }

    private fun httpClientNonAuth(): OkHttpClient.Builder {
        return OkHttpClient().newBuilder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain ->
                val original: Request = chain.request()

                val request: Request = original.newBuilder()
                    .method(original.method, original.body)
                    .build()

                chain.proceed(request)
            })
    }

    fun nonAuthorizedApiClient(baseUrl: String): T {
        val okHttpClient = httpClientNonAuth().build()

        val retrofitBuilder = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            client(okHttpClient)
            addConverterFactory(GsonConverterFactory.create())
        }

        val retrofit = retrofitBuilder.build()
        return retrofit.create(classT)
    }
}