package com.vuukle.webview.manager.url

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vuukle.webview.helper.UrlHelper
import com.vuukle.webview.manager.auth.AuthManager

class UrlManager(activity: AppCompatActivity) {

    private val authManager = AuthManager(activity)

    fun getCommentsUrl(): String {

        var url = "https://cdntest.vuukle.com/amp.html?url=https://romantic-villani-2fc571.netlify.app&host=romantic-villani-2fc571.netlify.app&id=123ggg45gerrge09876&apiKey=664e0b85-5b2c-4881-ba64-3aa9f992d01c&title=Aryaasdas123ff&title=test&img=https://pixabay.com/en/image-statue-brass-child-art-1465348"

        if(authManager.isLoggedIn()){
            url = url.plus("&sso=true&loginToken=${authManager.getToken()}")
        }

        return url
    }

    fun getPowerBarUrl(): String {
        return "https://cdntest.vuukle.com/amp-sharebar.html?amp=false&apiKey=80355907-6cca-4663-97c0-081020788dd7&host=angry-hermann-1e0239.netlify.app&id=123ggg45gerrge09876&img=https%3A%2F%2Fwww.gettyimages.ie%2Fgi-resources%2Fimages%2FHomepage%2FHero%2FUK%2FCMS_Creative_164657191_Kingfisher.jpg&title=test&url=https%3A%2F%2Frelaxed-beaver-76304e.netlify.app%2F&tags=123&author=123&lang=en&gr=false&darkMode=false&defaultEmote=1&items=&mode=horizontal&comments=true&emotes=true"
    }

    fun getAllUrls(): ArrayList<String>{
        val urls = ArrayList<String>()
        urls.add(UrlHelper.getHostUrl(getCommentsUrl()))
        urls.add(UrlHelper.getHostUrl(getPowerBarUrl()))
        urls.add("https://news.vuukle.com")
        return urls
    }
}