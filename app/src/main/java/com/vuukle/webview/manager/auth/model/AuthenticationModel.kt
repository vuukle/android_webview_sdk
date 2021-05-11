package com.vuukle.webview.manager.auth.model

import com.google.gson.annotations.SerializedName

data class AuthenticationModel(
    @SerializedName("email")
    val email: String,
    @SerializedName("public_key")
    val publicKey: String,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("username")
    val username: String
)