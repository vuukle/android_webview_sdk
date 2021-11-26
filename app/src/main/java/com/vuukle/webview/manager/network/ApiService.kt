package com.vuukle.webview.manager.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("login/auth/facebookLogin")
    suspend fun loginViaFacebook(@Query("token") token: String): Response<Any>
}