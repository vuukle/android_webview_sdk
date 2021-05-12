package com.vuukle.webview.utils

import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.RelativeLayout.TRUE
import com.vuukle.webview.MainActivity

class Dialog(private val context: MainActivity) {

    private var dialog: AlertDialog? = null
    private var openDialog = true
    private var wrapper: RelativeLayout? = null
    private var popup: WebView? = null
    private var webView: WebView? = null
    private var onCloseListener: DialogCancelListener? = null

    @JvmField
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    @JvmField
    var mUploadMessage: ValueCallback<Uri>? = null
    var openSite: OpenSite? = null
    private val openPhoto = OpenPhoto()

    private var progressBar: ProgressBar? = null
    fun openDialog(popup: WebView?) {
        this.popup = popup
        initLinearLayout()
    }

    fun openDialogOther(url: String?) {

        openSite = OpenSite(context)
        popup = WebView(context)
        popup!!.settings.pluginState = WebSettings.PluginState.ON
        popup!!.settings.setSupportMultipleWindows(true)
        popup!!.settings.domStorageEnabled = true
        popup!!.settings.javaScriptEnabled = true
        popup!!.settings.builtInZoomControls = true
        popup!!.settings.setAppCacheEnabled(true)
        popup!!.settings.loadsImagesAutomatically = true
        popup!!.settings.userAgentString = System.getProperty("http.agent")
                ?: "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        popup!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        if (url == null) {
            //TODO: Important log
            return
        }

        when {
            url.contains("whatsapp://send") -> {
                openSite!!.openWhatsApp(url, popup!!)
            }
            url.contains("fb-messenger") -> {
                openSite!!.openMessenger(url)
            }
            else -> {
                popup!!.loadUrl(url)
            }
        }

        popup!!.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else if (url.contains("whatsapp://send") || url.contains("fb-messenger") && popup != null) {
                    openSite!!.openWhatsApp(url, popup!!)
                    openSite!!.openMessenger(url)
                } else if (url.contains("tg:msg_url")) {
                    openSite!!.openApp(url)
                } else if (url.contains(MainActivity.CONSENT)) {
                    showLoader(true)
                    popup!!.loadUrl(url)
                    Log.i(MainActivity.TAG, "Clicked url: $url")
                } else {
                    showLoader(true)
                    popup!!.loadUrl(url)
                }

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                showLoader(false)
            }
        }

        popup?.webChromeClient = webChromeClient
        initLinearLayout()
    }

    private fun initLinearLayout() {

        if (openDialog) {
            openDialog = false
            wrapper = RelativeLayout(context)
            wrapper?.minimumHeight = MATCH_PARENT
            val keyboardHack = EditText(context)
            keyboardHack.visibility = View.GONE
            wrapper?.addView(popup, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            wrapper?.addView(keyboardHack, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            initDialog(wrapper)
        }
        initProgressBar()
    }

    private fun showLoader(show: Boolean) {
        if (show) {
            progressBar?.visibility = View.VISIBLE
        } else {
            progressBar?.visibility = View.GONE
        }
    }

    private fun initProgressBar() {

        // add progress bar
        progressBar = ProgressBar(context)
        val lp = RelativeLayout.LayoutParams(
                100,
                100
        )
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE)
        lp.setMargins(50, 50, 50, 50)
        progressBar!!.layoutParams = lp
        progressBar!!.tag = "progressBar"
        if (wrapper?.findViewWithTag<ProgressBar>("progressBar") == null) {
            wrapper?.addView(progressBar)
        }
    }

    private fun initDialog(wrapper: RelativeLayout?) {

        val builder = AlertDialog.Builder(context)
        builder.setNegativeButton("close") { v: DialogInterface?, l: Int ->
            context.reloadView()
            close()
        }
        builder.setView(wrapper)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.setOnKeyListener { _: DialogInterface?, keyCode: Int, event: KeyEvent? ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                context.reloadView()
                back()
            }
            true
        }
        dialog?.show()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    fun close() {
        if (dialog != null) {
            wrapper!!.removeView(popup)
            dialog!!.dismiss()
            openDialog = true
            dialog = null
            popup!!.destroy()
            popup = null
        }

        onCloseListener?.onClosed()
    }

    fun addCloseListener(closeListener: DialogCancelListener) {
        this.onCloseListener = closeListener
    }

    fun back() {
        if (dialog != null) if (popup != null && popup!!.canGoBack()) popup!!.goBack() else close()
    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {

            val transport = resultMsg.obj as WebViewTransport
            view.settings.setSupportMultipleWindows(true)
            webView = WebView(context)
            transport.webView = webView
            resultMsg.sendToTarget()
            webView?.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                    if (url.contains("mailto:to") || url.contains("mailto:")) {
                        openSite!!.openApp(url)
                    } else if (url.contains("whatsapp://send") || url.contains("fb-messenger") && popup != null) {
                        openSite!!.openWhatsApp(url, popup!!)
                        openSite!!.openMessenger(url)
                    } else if (url.contains("tg:msg_url")) {
                        openSite!!.openApp(url)
                    } else if (url.contains(MainActivity.CONSENT)) {
                        showLoader(true)
                        popup!!.loadUrl(url)
                        Log.i(MainActivity.TAG, "Clicked url: $url")
                    } else {
                        showLoader(true)
                        popup!!.loadUrl(url)
                    }
                    return true
                }
            }
            return true
        }

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            uploadMessage = filePathCallback
            openPhoto.selectImage(context){
                uploadMessage?.onReceiveValue(arrayOf())
                uploadMessage = null
            }
            return true
        }
    }

    companion object {
        const val CAMERA_PERMISSION = 2
    }
}