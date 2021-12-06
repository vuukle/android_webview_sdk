package com.vuukle.webview

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.vuukle.webview.ext.needOpenWithOther
import com.vuukle.webview.helper.AnimationHelper
import com.vuukle.webview.manager.auth.AuthManager
import com.vuukle.webview.manager.url.UrlManager
import com.vuukle.webview.utils.*
import java.util.*
import android.webkit.WebViewClient
import com.facebook.*

import com.facebook.share.Sharer
import com.vuukle.webview.helper.UrlHelper
import java.net.URLDecoder
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog

class MainActivity : AppCompatActivity(), ListenerModalWindow, PermissionListener {

    var fbShareDialog: ShareDialog? = null
    var mFacebookCallbackManager: CallbackManager? = null

    // Auth Manager
    private val authManager = AuthManager(this)
    var fbProcessRuning = false

    //URL manager for get urls loading into WebView
    private val urlManager = UrlManager(this)

    private val PERMISSION_REQUEST_CODE = 200

    var popup: WebView? = null

    //WebView
    var mWebViewComments: VuukleWebView? = null
    var mWebViewPowerBar: VuukleWebView? = null
    var mContentContainer: ScrollView? = null
    private lateinit var loginSSOButton: Button
    private lateinit var logoutSSOButton: Button
    var mContainer: LinearLayout? = null
    var openSite: OpenSite? = null
    var dialog: Dialog? = null

    @JvmField
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val openPhoto = OpenPhoto()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleOnCreate()
        setOnScrollListener()
        createActionBar()
        initOnClicks()
        initFacebook()
    }

    private fun setOnScrollListener() {

        var animationLocked = false

        mWebViewComments?.setOnTouchListener {
            animationLocked =
                !(it?.actionMasked == MotionEvent.ACTION_UP || it?.actionMasked == MotionEvent.ACTION_CANCEL)
            if (!animationLocked) checkStatusBarState(mWebViewComments?.scrollY ?: 0)
        }
        var isScrollable = true
        mWebViewComments?.setOnTouchListener {
            isScrollable = true
        }
        mWebViewComments?.setOnScrollChangeListener { l, t, oldl, oldt ->

            val contentHeight =
                mWebViewComments?.contentHeight ?: mWebViewComments?.measuredHeight ?: 0
            val delta = contentHeight + 100

            if (!isScrollable) {
                mWebViewComments?.scrollY = delta
            }

            if (t >= delta && mWebViewPowerBar?.tag == "1") {
                mWebViewComments?.scrollY = delta
                isScrollable = false
            } else {
                if (!animationLocked) checkStatusBarState(t)
            }
        }
    }

    private fun checkStatusBarState(scrollY: Int) {

        if (scrollY > 0 && mWebViewPowerBar?.tag == "1") {
            mWebViewPowerBar?.let {
                AnimationHelper.moveToY(it, 300, -it.height.toFloat()) {
                    AnimationHelper.changeMarginTop(mWebViewComments!!, 300, 0) {
                        mWebViewPowerBar?.tag = "0"
                    }
                }
            }

        } else if (scrollY <= 0 && mWebViewPowerBar?.tag == "0") {
            mWebViewPowerBar?.let {
                AnimationHelper.moveToY(it, 300, 0F) {
                    AnimationHelper.changeMarginTop(mWebViewComments!!, 300, it.height) {
                        mWebViewPowerBar?.tag = "1"
                    }
                }
            }
        }
    }

    private fun createActionBar() {

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_action_bar_layout)
        val view = supportActionBar!!.customView
        loginSSOButton = view.findViewById<Button>(R.id.login_by_sso)
        logoutSSOButton = view.findViewById<Button>(R.id.logout_by_sso)
    }

    private fun initOnClicks() {

        dialog?.addCloseListener() {
            //mWebViewComments?.reload()
        }

        getSharedPreferences("asd", Context.MODE_PRIVATE)

        loginSSOButton.setOnClickListener {
            loginBySSO("sometempmail@yopmail.com", "Sample User Name")
        }

        logoutSSOButton.setOnClickListener {
            logout()
        }
    }

    // Login user by SSO using email and username
    private fun loginBySSO(email: String, userName: String) {
        // Login user
        authManager.login(email, userName)
        // Clear browser history
        mWebViewComments?.clearHistory()
        // Logout facebook
        LoginManager.getInstance().logOut()
        // Reload WebView using urlManager
        mWebViewComments?.loadUrl(urlManager.getCommentsUrl())
    }

    // Logout user SSO
    private fun logout() {
        // Logout user
        authManager.logout()
        // Clear all cookies
        CookieManager.getInstance().removeAllCookie()
        // Clear history
        mWebViewComments?.clearHistory()
        // Reload WebView using urlManager
        mWebViewComments?.loadUrl(urlManager.getCommentsUrl())
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                // main logic
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel(
                            "You need to allow access permissions"
                        ) { dialog, which ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {

        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun handleOnCreate() {

        // debug test webView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        //initialising views
        setContentView(R.layout.activity_main)
        mWebViewComments = findViewById(R.id.activity_main_webview_comments)
        mWebViewPowerBar = findViewById(R.id.activity_main_webview_powerbar)
        mWebViewPowerBar?.tag = "1"
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
        mWebViewComments?.loadUrl(urlManager.getCommentsUrl())
        mWebViewPowerBar?.loadUrl(urlManager.getPowerBarUrl())
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

        mWebViewPowerBar?.settings?.javaScriptEnabled = true
        mWebViewPowerBar?.settings?.domStorageEnabled = true
        mWebViewPowerBar?.settings?.setSupportMultipleWindows(false)
        mWebViewPowerBar?.settings?.setSupportZoom(false)
        mWebViewPowerBar?.settings?.allowFileAccess = true
        mWebViewPowerBar?.settings?.allowContentAccess = true
        mWebViewPowerBar?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        mWebViewPowerBar?.settings?.pluginState = WebSettings.PluginState.ON;
        mWebViewPowerBar?.webChromeClient = webChromeClient
        mWebViewPowerBar?.settings?.userAgentString = System.getProperty("http.agent")
            ?: "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        mWebViewPowerBar?.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //Clicked url
                if (url.contains("whatsapp://send") || url.contains("https://web.whatsapp.com/send?text=") || url.contains(
                        "fb-messenger"
                    ) && popup != null
                ) {
                    openSite!!.openWhatsApp(url, mWebViewComments!!)
                } else if (url.contains("tg:msg_url")) {
                    openSite!!.openApp(url)
                } else if (openSite!!.isOpenSupportInBrowser(url)) {
                    openSite!!.openPrivacyPolicy(url)
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else {
                    val urlDecoded = URLDecoder.decode(url, "UTF-8")
                    val originUrlDecoded = URLDecoder.decode(urlManager.getPowerBarUrl(), "UTF-8")

                    if (!url.needOpenWithOther() && !urlDecoded.equals(originUrlDecoded)) {
                        dialog!!.openDialogOther(url)
                    } else {
                        return false
                    }
                }
                return true
            }
        }

        mWebViewComments?.settings?.javaScriptEnabled = true
        mWebViewComments?.settings?.domStorageEnabled = true
        mWebViewComments?.settings?.setSupportZoom(false)
        mWebViewComments?.settings?.userAgentString = System.getProperty("http.agent")
            ?: "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        mWebViewComments?.settings?.allowFileAccess = true
        mWebViewComments?.settings?.allowContentAccess = true
        mWebViewComments?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        mWebViewComments!!.webChromeClient = webChromeClient
        mWebViewComments?.settings?.pluginState = WebSettings.PluginState.ON;
        mWebViewComments?.settings?.mediaPlaybackRequiresUserGesture = false;
        mWebViewComments?.settings?.setSupportMultipleWindows(true)

        mWebViewComments!!.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mWebViewPowerBar?.reload()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                //Clicked url
                if (url.contains("whatsapp://send") ||
                    url.contains("https://web.whatsapp.com/send?text=") ||
                    url.contains("fb-messenger") && popup != null
                ) {
                    openSite!!.openWhatsApp(url, mWebViewComments!!)
                } else if (url.contains("tg:msg_url")) {
                    openSite!!.openApp(url)
                } else if (openSite!!.isOpenSupportInBrowser(url)) {
                    openSite!!.openPrivacyPolicy(url)
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else {

                    val urlDecoded = URLDecoder.decode(url, "UTF-8")
                    val originUrlDecoded = URLDecoder.decode(urlManager.getCommentsUrl(), "UTF-8")

                    if (!url.needOpenWithOther() && !urlDecoded.equals(originUrlDecoded)) {
                        dialog!!.openDialogOther(url)
                    } else {
                        return false
                    }
                }
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)
        mFacebookCallbackManager?.onActivityResult(requestCode, resultCode, intent)

        if (CAMERA_PERMISSION == resultCode && requestCode == Activity.RESULT_OK)
            openPhoto.selectImage(this@MainActivity)

        if (requestCode == REQUEST_SELECT_FILE) {
            val result =
                if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            result?.let {
                dialog?.uploadMessage?.onReceiveValue(arrayOf(result))
                dialog?.uploadMessage = null
                uploadMessage?.onReceiveValue(arrayOf(result))
                uploadMessage = null
            }
            return
        }
    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            uploadMessage = filePathCallback
            openPhoto.selectImage(context = this@MainActivity) {
                uploadMessage?.onReceiveValue(arrayOf())
                uploadMessage = null
            }
            return true;
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {

            if (consoleMessage.message().contains("logout-clicked")) {
                logout()
            } else if (consoleMessage.message().contains("sso-sign-in")) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Sign in button clicked")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ -> }
                    .create()
                    .show()
            }
            return super.onConsoleMessage(consoleMessage)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
            request?.grant(request.resources)
        }

        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {

            popup = WebView(this@MainActivity)
            popup!!.settings.javaScriptEnabled = true
            popup!!.settings.domStorageEnabled = true
            popup!!.settings.pluginState = WebSettings.PluginState.ON
            popup!!.settings.setSupportMultipleWindows(true)
            popup!!.layoutParams = view.layoutParams
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(popup, true)
            };
            popup!!.settings.userAgentString = System.getProperty("http.agent")
                ?: "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
            val urlLast = arrayOf("")
            popup!!.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    dialog?.showLoader(false)
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                    if(fbProcessRuning) {
                        emptyDialog()
                        return false
                    }

                    val isOpenApp =
                        when {
                            (url.contains("whatsapp://send") || url.contains("https://web.whatsapp.com/send?text=") || url.contains(
                                "fb-messenger"
                            ) && popup != null
                                    ) -> {
                                openSite!!.openWhatsApp(url, mWebViewComments!!)
                                true
                            }
                            url.contains("tg:msg_url") -> {
                                openSite!!.openApp(url)
                                true
                            }
                            openSite!!.isOpenSupportInBrowser(url) -> {
                                openSite!!.openPrivacyPolicy(url)
                                true
                            }
                            url.contains("mailto:to") || url.contains("mailto:") -> {
                                openSite!!.openApp(url)
                                true
                            }
                            url.contains("vuukle.com/login/auth/facebook") -> {
                                if (!fbProcessRuning) {
                                    if (AccessToken.getCurrentAccessToken() != null) {
                                        LoginManager.getInstance().logOut();
                                    }
                                    LoginManager.getInstance().logInWithReadPermissions(
                                        this@MainActivity,
                                        Arrays.asList("public_profile", "email")
                                    );
                                    fbProcessRuning = true
                                }
                                true
                            }
                            url.contains("facebook.com/share")-> {
                                val queryData = UrlHelper.getQueryData(url)
                                val contentBuilder = ShareLinkContent.Builder()
                                if(queryData.containsKey("u")){
                                    contentBuilder.setContentUrl(Uri.parse(queryData["u"]))
                                }
                                if(queryData.containsKey("quote")){
                                    contentBuilder.setQuote(queryData["quote"])
                                }
                                fbShareDialog?.show(contentBuilder.build())
                                fbProcessRuning = true
                                true
                            }
                            else -> false
                        }

                    if (isOpenApp) {
                        emptyDialog()
                        return false
                    }

                    if (popup != null) {

                        if (url.contains(AUTH) || url.contains(CONSENT)) {
                            if (url.contains(errorTwitter))
                                dialog!!.close()
                            else {
                                dialog?.showLoader(true)
                                popup?.loadUrl(url)
                                dialog?.openDialog(popup)
                                if (url.contains(CONSENT)) hideKeyboard()
                            }
                        } else {
                            if (!url.needOpenWithOther()) {
                                dialog?.openDialogOther(url)
                            }
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

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    uploadMessage = filePathCallback
                    openPhoto.selectImage(context = this@MainActivity) {
                        uploadMessage?.onReceiveValue(arrayOf())
                        uploadMessage = null
                    }
                    return false;
                }

                override fun onCloseWindow(window: WebView) {
                    super.onCloseWindow(window)
                    dialog!!.close()
                    //if (mWebViewComments != null) mWebViewComments!!.reload()
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
        //mWebViewComments!!.reload()
    }

    companion object {
        const val TAG = "MainActivity"

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

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Please accept camera permission", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {

    }

    private fun initFacebook() {

        this.mFacebookCallbackManager = CallbackManager.Factory.create()

        fbShareDialog = ShareDialog(this)
        fbShareDialog!!.registerCallback(mFacebookCallbackManager, object : FacebookCallback<Sharer.Result?> {

            override fun onSuccess(result: Sharer.Result?) {
                emptyDialog()
                fbProcessRuning = false
            }

            override fun onCancel() {
                emptyDialog()
                Toast.makeText(this@MainActivity, "Cancel", Toast.LENGTH_LONG).show()
                fbProcessRuning = false
            }

            override fun onError(error: FacebookException) {
                emptyDialog()
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                fbProcessRuning = false
            }
        })

        LoginManager.getInstance().registerCallback(mFacebookCallbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    loginResult?.accessToken?.let {
                        authManager.loginViaFacebook(it.token) { token ->
                            if(token != null){
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                val tokenCookie = "token=$token";
                                authManager.setAutorizationCookieForVuukle(urlManager, tokenCookie)
                                mWebViewPowerBar?.loadUrl(mWebViewPowerBar!!.url ?: urlManager.getPowerBarUrl())
                                mWebViewComments?.loadUrl(mWebViewComments!!.url ?: urlManager.getCommentsUrl())
                                dialog?.popup?.loadUrl(dialog?.popup?.url!!)
                            }else{
                                Toast.makeText(
                                    this@MainActivity,
                                    "Can not login",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } ?: run {
                        Toast.makeText(this@MainActivity, "Can not login", Toast.LENGTH_LONG).show()
                    }
                    fbProcessRuning = false
                }

                override fun onCancel() {
                    emptyDialog()
                    Toast.makeText(this@MainActivity, "Can not login", Toast.LENGTH_LONG).show()
                    fbProcessRuning = false
                }

                override fun onError(exception: FacebookException) {
                    emptyDialog()
                    Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
                    fbProcessRuning = false
                }
            })
    }

    fun emptyDialog(){
        popup?.clearView()
        popup?.loadUrl("about:blank")
        dialog?.reset()
    }
}