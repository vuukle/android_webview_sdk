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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.vuukle.webview.ext.needOpenWithOther
import com.vuukle.webview.manager.auth.AuthManager
import com.vuukle.webview.manager.url.UrlManager
import com.vuukle.webview.utils.Dialog
import com.vuukle.webview.utils.ListenerModalWindow
import com.vuukle.webview.utils.OpenPhoto
import com.vuukle.webview.utils.OpenSite


class MainActivity : AppCompatActivity(), ListenerModalWindow, PermissionListener {

    // Auth Manager
    private val authManager = AuthManager(this)
    //URL manager for get urls loading into WebView
    private val urlManager = UrlManager(this)

    private val PERMISSION_REQUEST_CODE = 200

    var popup: WebView? = null

    //WebView
    var mWebViewComments: WebView? = null
    var mWebViewPowerBar: WebView? = null
    var mContainer: LinearLayout? = null
    var openSite: OpenSite? = null
    var dialog: Dialog? = null
    @JvmField
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val openPhoto = OpenPhoto()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleOnCreate()
        initOnClicks();
    }

    private fun initOnClicks() {

        dialog?.addCloseListener() {
            mWebViewComments?.reload()
        }

        getSharedPreferences("asd", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.login_by_sso).setOnClickListener {
            loginBySSO("sometempmail@yopmail.com", "Sample User Name")
        }

        findViewById<Button>(R.id.logout_by_sso).setOnClickListener {
            logoutSSO()
        }
    }

    private fun loginBySSO(email: String, userName: String) {

        authManager.login(email, userName)
        mWebViewComments?.clearHistory()
        mWebViewComments?.loadUrl(urlManager.getCommentsUrl())
    }

    private fun logoutSSO(){

        authManager.logout()
        CookieManager.getInstance().removeAllCookie()
        mWebViewComments?.clearHistory()
        mWebViewComments?.loadUrl(urlManager.getCommentsUrl())
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                // main logic
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel("You need to allow access permissions"
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

        mWebViewPowerBar!!.settings.javaScriptEnabled = true
        mWebViewPowerBar!!.settings.domStorageEnabled = true
        mWebViewPowerBar!!.settings.setSupportMultipleWindows(true)
        mWebViewPowerBar!!.webChromeClient = webChromeClient
        mWebViewPowerBar!!.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //Clicked url
                Log.d(TAG, "Clicked url: $url")
                if (openSite!!.isOpenSupportInBrowser(url)) {
                    openSite!!.openPrivacyPolicy(url)
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else {
                    if (!url.needOpenWithOther()) {
                        dialog!!.openDialogOther(url)
                    }
                }
                return true
            }
        }


        mWebViewComments?.settings?.javaScriptEnabled = true
        mWebViewComments?.settings?.domStorageEnabled = true
        mWebViewComments?.settings?.setSupportZoom(false)
        mWebViewComments?.settings?.allowFileAccess = true
        mWebViewComments?.settings?.allowContentAccess = true
        mWebViewComments?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        mWebViewComments!!.webChromeClient = webChromeClient
        mWebViewComments?.settings?.pluginState = WebSettings.PluginState.ON;
        mWebViewComments?.settings?.mediaPlaybackRequiresUserGesture = false;
        mWebViewComments!!.webViewClient = object : WebViewClient() {


            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //Clicked url
                Log.i(TAG, "Clicked url: $url")
                if (openSite!!.isOpenSupportInBrowser(url)) {
                    openSite!!.openPrivacyPolicy(url)
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite!!.openApp(url)
                } else {
                    if (!url.needOpenWithOther()) {
                        dialog!!.openDialogOther(url)
                    }
                }
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (CAMERA_PERMISSION == resultCode && requestCode == Activity.RESULT_OK) openPhoto.selectImage(this@MainActivity)


        if (requestCode == REQUEST_SELECT_FILE) {
            val result = if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
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

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            uploadMessage = filePathCallback
            openPhoto.selectImage(context = this@MainActivity){
                uploadMessage?.onReceiveValue(arrayOf())
                uploadMessage = null
            }
            return true;
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
           /* Log.d("consoleJs", consoleMessage.message())
            //Listening for console message that contains "Comments initialized!" string
            if (consoleMessage.message().contains("Comments initialized!")) {
                //signInUser(name, email) - javascript function implemented on a page
                mWebViewComments!!.loadUrl("javascript:signInUser('$name', '$email')")
            }*/
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            popup = WebView(this@MainActivity)
            popup!!.settings.javaScriptEnabled = true
            popup!!.settings.domStorageEnabled = true
            popup!!.settings.pluginState = WebSettings.PluginState.ON
            popup!!.settings.setSupportMultipleWindows(true)
            popup!!.layoutParams = view.layoutParams
            popup!!.settings.userAgentString = popup!!.settings.userAgentString.replace("; wv", "")
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

                            if (!url.needOpenWithOther()) {
                                dialog!!.openDialogOther(url)
                                dialog!!
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

                override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                    uploadMessage = filePathCallback
                    openPhoto.selectImage(context = this@MainActivity){
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

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Please accept camera permission", Toast.LENGTH_LONG).show()
    }
}