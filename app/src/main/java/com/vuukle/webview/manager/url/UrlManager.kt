package com.vuukle.webview.manager.url

import androidx.appcompat.app.AppCompatActivity
import com.vuukle.webview.manager.auth.AuthManager

class UrlManager(activity: AppCompatActivity) {

    private val authManager = AuthManager(activity)

    fun getCommentsUrl(): String {

        val url = "https://cdn.vuukle.com/amp.html?url=https%3A%2F%2Fwww.prowrestling.com%2Fimpact-wrestling-results-1282020%2F&host=prowrestling.com&id=1196371&apiKey=46489985-43ef-48ed-9bfd-61971e6af217&img=https%3A%2F%2Fwww.prowrestling.com%2Fwp-content%2Fuploads%2F2020%2F12%2FKenny-Omega-Impact-Wrestling.jpeg&title=IMPACT%2BWrestling%2BResults%2B%252812%252F8%2529%253A%2BKenny%2BOmega%2BSpeaks%252C%2BKnockouts%2BTag%2BTournament%2BContinues%2521&tags=Featured"

        return if(authManager.isLoggedIn()){
            url.plus("&sso=true&loginToken=${authManager.getToken()}")
        }else {
            url
        }
    }

    fun getPowerBarUrl(): String {
        return "https://cdntest.vuukle.com/widgets/powerbar.html?amp=false&apiKey=664e0b85-5b2c-4881-ba64-3aa9f992d01c&host=relaxed-beaver-76304e.netlify.com&articleId=Index&img=https%3A%2F%2Fwww.gettyimages.ie%2Fgi-resources%2Fimages%2FHomepage%2FHero%2FUK%2FCMS_Creative_164657191_Kingfisher.jpg&title=Index&url=https%3A%2F%2Frelaxed-beaver-76304e.netlify.app%2F&tags=123&author=123&lang=en&gr=false&darkMode=false&defaultEmote=1&items=&standalone=0&mode=horizontal"
    }
}