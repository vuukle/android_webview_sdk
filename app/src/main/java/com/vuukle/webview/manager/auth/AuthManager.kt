package com.vuukle.webview.manager.auth

import android.util.Base64.DEFAULT
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.vuukle.webview.BuildConfig
import com.vuukle.webview.manager.auth.model.AuthenticationModel
import com.vuukle.webview.manager.network.ApiService
import com.vuukle.webview.manager.network.BaseApiClient
import kotlinx.coroutines.runBlocking
import litparty.app.manager.storage.StorageImpl
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class AuthManager(activity: AppCompatActivity) {

    /**
     *  Returning non authorization header Client
     */
    private fun nonAuthorizedApiClient(): ApiService {
        return BaseApiClient(ApiService::class.java)
            .nonAuthorizedApiClient("https://cdn.vuukle.com/")
    }

    companion object {
        private const val tokenKey = "token"
    }

    private val privateKey = BuildConfig.PUBLISHER_PRIVATE_KEY
    private val publicKey = BuildConfig.PUBLISHER_PUBLIC_KEY
    private val storageManager = StorageImpl(activity)

    fun loginViaFacebook(fbToken: String) = runBlocking {

        val response = nonAuthorizedApiClient().loginViaFacebook(fbToken)
        println()
    }

    /**
     * Login by user email and Name
     */
    @Synchronized
    fun login(email: String?, userName: String?): Boolean {

        if (email.isNullOrEmpty() || userName.isNullOrEmpty()) return false

        val authModel = generateAuthenticationModel(email, userName)
        val authJsonString = Gson().toJson(authModel)

        val authToken = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(authJsonString.toByteArray(Charsets.UTF_8))
        } else {
            android.util.Base64.encodeToString(authJsonString.toByteArray(Charsets.UTF_8), DEFAULT)
        }

        storageManager.putData(tokenKey, authToken)
        return true
    }

    /**
     * Returns user login state
     */
    fun isLoggedIn(): Boolean {
        val token = storageManager.getStringData(tokenKey)
        return !(token.isNullOrEmpty() || token.isBlank())
    }

    /**
     * Logout
     */
    fun logout(){
        storageManager.putData(tokenKey, "")
    }

    /**
     * Get user token
     */
    fun getToken(): String? {
        return storageManager.getStringData(tokenKey)
    }

    private fun generateAuthenticationModel(email: String, userName: String): AuthenticationModel {

        return AuthenticationModel(
                email = email,
                username = userName,
                publicKey = publicKey,
                signature = generateSignature(email)
        )
    }

    private fun generateSignature(email: String): String{

        val signatureString = email.plus("-").plus(privateKey)
        val upper = encodeSignature(signatureString).toUpperCase(Locale.ROOT)
        return upper
    }

    private fun encodeSignature(stringHash: String): String {

        var token: String = ""

        try {
            val md = MessageDigest.getInstance("SHA-512")
            val bytes = md.digest(stringHash.toByteArray(StandardCharsets.UTF_8))
            token = bytes.map { Integer.toHexString(0xFF and it.toInt()) }
                    .map { if (it.length < 2) "0$it" else it }
                    .fold("", { acc, d -> acc + d })
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return token
    }
}