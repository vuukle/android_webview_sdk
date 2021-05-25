package com.vuukle.webview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class VuukleWebView(context: Context, attrs: AttributeSet?) : WebView(context, attrs) {

    private var onScrollChangeListener: ((l: Int, t: Int, oldl: Int, oldt: Int) -> Unit)? = null
    private var onTouchListener: ((ev: MotionEvent?) -> Unit)? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollChangeListener?.invoke(l, t, oldl, oldt)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        onTouchListener?.invoke(ev)
        return super.onTouchEvent(ev)
    }

    fun setOnTouchListener(listener: (ev: MotionEvent?) -> Unit){
        onTouchListener = listener
    }

    fun setOnScrollChangeListener(listener: (l: Int, t: Int, oldl: Int, oldt: Int) -> Unit){
        onScrollChangeListener = listener
    }
}