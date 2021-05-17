package com.vuukle.webview.manager.url

import androidx.appcompat.app.AppCompatActivity
import com.vuukle.webview.manager.auth.AuthManager

class UrlManager(activity: AppCompatActivity) {

    private val authManager = AuthManager(activity)

    fun getCommentsUrl(): String {

        val url = "https://cdn.vuukle.com/amp.html?url=https%3A%2F%2Fangry-hermann-1e0239.netlify.app%2F&host=angry-hermann-1e0239.netlify.app&id=123ggg45gerrge09876&apiKey=80355907-6cca-4663-97c0-081020788dd7&img=https%3A%2F%2Fpixabay.com%2Fen%2Fimage-statue-brass-child-art-1465348&title=test&tags=Featured"

        return if(authManager.isLoggedIn()){
            url.plus("&sso=true&loginToken=${authManager.getToken()}")
        }else {
            url
        }
    }

    fun getPowerBarUrl(): String {
        return "https://cdn.vuukle.com/amp-sharebar.html?amp=false&apiKey=80355907-6cca-4663-97c0-081020788dd7&host=angry-hermann-1e0239.netlify.app&id=123ggg45gerrge09876&img=https%3A%2F%2Fwww.gettyimages.ie%2Fgi-resources%2Fimages%2FHomepage%2FHero%2FUK%2FCMS_Creative_164657191_Kingfisher.jpg&title=test&url=https%3A%2F%2Frelaxed-beaver-76304e.netlify.app%2F&tags=123&author=123&lang=en&gr=false&darkMode=false&defaultEmote=1&items=&mode=horizontal&comments=true&emotes=true"
    }
}