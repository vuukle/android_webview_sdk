package com.vuukle.webview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.vuukle.webview.utils.Dialog
import com.vuukle.webview.utils.ListenerModalWindow
import com.vuukle.webview.utils.OpenPhoto
import com.vuukle.webview.utils.OpenSite

class MainActivity : AppCompatActivity(), ListenerModalWindow, PermissionListener {
    //URL for loading into WebView
    private val COMMENTS_URL = "https://cdntest.vuukle.com/amp.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&id=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1"

    //login name
    var name = "Alex"

    //login email
    var email = "email@test.com"
    var popup: WebView? = null

    //WebView
    var mWebViewComments: WebView? = null
    var mContainer: FrameLayout? = null
    var openSite: OpenSite? = null
    var dialog: Dialog? = null
    private val openPhoto = OpenPhoto()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(this)
                .check()
    }

    private fun handleOnCreate() {
        // debug test webView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        //initialising views
        setContentView(R.layout.activity_main)
        mWebViewComments = findViewById(R.id.activity_main_webview_comments)
        mContainer = findViewById(R.id.container)
        openSite = OpenSite(this)
        dialog = Dialog(this)
        //initialising webView
        configWebView()

        //cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebViewComments, true)
        } else CookieManager.getInstance().setAcceptCookie(true)
        //load url to display in webView
        mWebViewComments?.loadUrl(COMMENTS_URL)
    }

    override fun onBackPressed() {
        if (popup != null && popup!!.parent != null) {
            mContainer!!.removeView(popup)
            popup!!.destroy()
        } else {
            mWebViewComments!!.goBack()
        }
    }

    private fun configWebView() {
        //javascript support
        mWebViewComments!!.settings.javaScriptEnabled = true
        mWebViewComments!!.settings.domStorageEnabled = true
        mWebViewComments!!.settings.setSupportMultipleWindows(true)
        mWebViewComments!!.webChromeClient = webChromeClient
        mWebViewComments!!.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                return super.shouldInterceptRequest(view, url)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                return super.shouldOverrideKeyEvent(view, event)
            }

            override fun onSafeBrowsingHit(view: WebView?, request: WebResourceRequest?, threatType: Int, callback: SafeBrowsingResponse?) {
                super.onSafeBrowsingHit(view, request, threatType, callback)
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }

            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                return super.onRenderProcessGone(view, detail)
            }

            override fun onReceivedLoginRequest(view: WebView?, realm: String?, account: String?, args: String?) {
                super.onReceivedLoginRequest(view, realm, account, args)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                super.onScaleChanged(view, oldScale, newScale)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                super.onUnhandledKeyEvent(view, event)
            }

            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                super.onReceivedClientCertRequest(view, request)
            }

            override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
            }

            override fun onTooManyRedirects(view: WebView?, cancelMsg: Message?, continueMsg: Message?) {
                super.onTooManyRedirects(view, cancelMsg, continueMsg)
            }

            override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
                super.onFormResubmission(view, dontResend, resend)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //Clicked url
                Log.d(TAG, "Clicked url: $url")
                if (openSite!!.isOpenSupportInBrowser(url)) {
                    openSite!!.openPrivacyPolicy(url)
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else {
                    dialog!!.openDialogOther(url)
                }
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (CAMERA_PERMISSION == resultCode && requestCode == Activity.RESULT_OK) openPhoto.selectImage(this@MainActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (dialog!!.uploadMessage == null) return
                if (intent == null) {
                    val intent1 = Intent()
                    intent1.data = openPhoto.imageUri
                    dialog!!.uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent1))
                } else dialog!!.uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
                dialog!!.uploadMessage = null
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == dialog!!.uploadMessage) return
            val result = if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            dialog!!.mUploadMessage!!.onReceiveValue(result)
            dialog!!.mUploadMessage = null
        }
    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.d("consoleJs", consoleMessage.message())
            //Listening for console message that contains "Comments initialized!" string
            if (consoleMessage.message().contains("Comments initialized!")) {
                //signInUser(name, email) - javascript function implemented on a page
                mWebViewComments!!.loadUrl("javascript:signInUser('$name', '$email')")
            }
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            popup = WebView(this@MainActivity)
            popup!!.settings.javaScriptEnabled = true
            popup!!.settings.pluginState = WebSettings.PluginState.ON
            popup!!.settings.setSupportMultipleWindows(true)
            popup!!.layoutParams = view.layoutParams
            popup!!.settings.setUserAgentString(popup!!.settings.userAgentString.replace("; wv", ""))
            val urlLast = arrayOf("")
            popup!!.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (popup != null) {
                        if (url.contains(AUTH) || url.contains(CONSENT)) {
                            if (url.contains(errorTwitter)) dialog!!.close() else {
                                popup!!.loadUrl(url)
                                dialog!!.openDialog(popup)
                                if (url.contains(CONSENT)) hideKeyboard()
                            }
                        } else {
                            dialog!!.openDialogOther(url)
                        }
                    }
                    checkConsent(url)
                    return true
                }

                private fun checkConsent(url: String) {
                    if (urlLast[0] == url) {
                        dialog!!.close()
                        popup!!.destroy()
                    } else {
                        urlLast[0] = url
                    }
                }
            }
            popup!!.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView) {
                    super.onCloseWindow(window)
                    dialog!!.close()
                    if (mWebViewComments != null) mWebViewComments!!.reload()
                    mContainer!!.removeView(window)
                }
            }
            val transport = resultMsg.obj as WebViewTransport
            transport.webView = popup
            resultMsg.sendToTarget()
            return true
        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun reloadView() {
        mWebViewComments!!.reload()
    }

    companion object {
        private const val TAG = "MainActivity"

        //Constant
        const val errorTwitter = "twitter/callback?denied"
        const val PRIVACY_POLICY = "https://docs.vuukle.com/"
        const val VUUKLE = "https://vuukle.com/"
        const val BLOG_VUUKLE = "https://blog.vuukle.com/"
        const val AUTH = "auth"
        const val CONSENT = "consent"
        const val REQUEST_SELECT_FILE = 1021
        const val FILE_CHOOSER_RESULT_CODE = 1
        const val CAMERA_PERMISSION = 2
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        handleOnCreate()
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Please accept camera permission", Toast.LENGTH_LONG).show()
    }
}