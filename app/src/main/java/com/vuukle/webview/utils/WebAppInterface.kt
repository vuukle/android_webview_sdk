package com.vuukle.webview.utils

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class WebAppInterface(
    private val wb: WebView
) {

    @JavascriptInterface
    fun postMessage(json: String) {
        val obj = JSONObject(json)
        val id = obj["id"]
    }

    private fun WebView.sendResult1(addr: String, id: Any) {
        val callback = "window.ethereum.sendResponse($id, [\"$addr\"])"
        evaluateJavascript(callback, null)
    }

    private fun WebView.sendResult2(addr: String, id: Any) {
        val callback = "window.ethereum.sendResponse($id, \"$addr\")"
        evaluateJavascript(callback, null)
    }

    private fun WebView.sendError(result: String, id: Any) {
        val callback = "window.ethereum.sendError($id, \"$result\")"
        evaluateJavascript(callback, null)
    }
}


