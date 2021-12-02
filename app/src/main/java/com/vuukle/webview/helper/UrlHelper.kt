package com.vuukle.webview.helper

import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.util.LinkedHashMap

object UrlHelper {

    fun getQueryData(url: String): LinkedHashMap<String, String> {

        val queryParams = LinkedHashMap<String, String>()

        if (url.isEmpty() || url.isBlank() || !url.contains("?")) return queryParams

        val urlInstance = URL(url)

        val query: String = urlInstance.query
        val pairs = query.split("&").toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if(idx == -1) continue
            queryParams.put(
                URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
            )
        }

        return queryParams
    }

    fun getHostUrl(urlString: String): String {
        return try {
            val url = URL(urlString);
            url.getProtocol() + "://" + url.getHost();
        } catch (e: MalformedURLException) {
            ""
        }
    }
}