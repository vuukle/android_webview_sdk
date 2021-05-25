package com.vuukle.webview.ext

import android.util.DisplayMetrics
import com.vuukle.webview.AppApplication
import com.vuukle.webview.utils.OpenSite
import kotlin.math.roundToInt

fun String.needOpenWithOther(): Boolean {

    if (this.contains("fb-messenger")) {
        OpenSite(AppApplication.mContext)
                .openMessenger(this)
        return true
    }

    val needWithOther = this.contains("mailto:to") ||
            this.contains("mailto:") ||
            this.contains("tg:msg_url") ||
            this.contains("share.flipboard.com")

    return if (!needWithOther) {
        false
    } else {
        OpenSite(AppApplication.mContext).openApp(this)
        true
    }
}