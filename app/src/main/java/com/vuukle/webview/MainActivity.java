package com.vuukle.webview;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vuukle.webview.utils.Dialog;
import com.vuukle.webview.utils.OpenPhoto;
import com.vuukle.webview.utils.OpenSite;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //URL for loading into WebView
    private final String COMMENTS_URL = "https://cdntest.vuukle.com/amp.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&id=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1";
    //login name
    String name = "Alex";
    //login email
    String email = "email@test.com";
    public WebView popup;
    //WebView
    public WebView mWebViewComments;
    public FrameLayout mContainer;
    public OpenSite openSite;
    public Dialog dialog;
    //Constant
    public static final String PRIVACY_POLICY = "https://docs.vuukle.com/";
    public static final String VUUKLE = "https://vuukle.com/";
    public static final String BLOG_VUUKLE = "https://blog.vuukle.com/";
    public static final String AUTH = "auth";
    public static final String CONSENT = "consent";
    private OpenPhoto openPhoto = new OpenPhoto();
    public static final int REQUEST_SELECT_FILE = 1021;
    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE = 1;
    public final static int CAMERA_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // debug test webView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //initialising views
        setContentView(R.layout.activity_main);
        mWebViewComments = findViewById(R.id.activity_main_webview_comments);
        mContainer = findViewById(R.id.container);
        openSite = new OpenSite(this);
        dialog = new Dialog(this);
        //initialising webView
        configWebView();

        //cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebViewComments, true);
        } else
            CookieManager.getInstance().setAcceptCookie(true);
        //load url to display in webView
        mWebViewComments.loadUrl(COMMENTS_URL);
    }

    @Override
    public void onBackPressed() {
        if (this.popup != null && this.popup.getParent() != null) {
            mContainer.removeView(popup);
            this.popup.destroy();
        } else {
            mWebViewComments.goBack();
        }
    }

    private void configWebView() {
        //javascript support
        mWebViewComments.getSettings().setJavaScriptEnabled(true);

        mWebViewComments.getSettings().setDomStorageEnabled(true);
        mWebViewComments.getSettings().setSupportMultipleWindows(true);

        mWebViewComments.setWebChromeClient(webChromeClient);
        mWebViewComments.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                //Clicked url
                Log.d(TAG, "Clicked url: " + url);
                if (openSite.isOpenSupportInBrowser(url)) {
                    openSite.openPrivacyPolicy(url);
                } else if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite.openEmail(url.replace("%20", " "));
                } else {
                    //Lets signInUser whenever url is clicked just for sample
                    openSite.openWhatsApp(url, view);
                    openSite.openMessenger(url);
                }
                //if u use super() it will load url into webView
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (CAMERA_PERMISSION == resultCode && requestCode == Activity.RESULT_OK)
            openPhoto.selectImage(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                if (intent == null) {
                    Intent intent1 = new Intent();
                    intent1.setData(openPhoto.getImageUri());
                    uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent1));
                } else
                    uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

    }

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d("consoleJs", consoleMessage.message());
            //Listening for console message that contains "Comments initialized!" string
            if (consoleMessage.message().contains("Comments initialized!")) {
                //signInUser(name, email) - javascript function implemented on a page
                mWebViewComments.loadUrl("javascript:signInUser('" + name + "', '" + email + "')");
            }
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            popup = new WebView(MainActivity.this);
            popup.getSettings().setJavaScriptEnabled(true);
            popup.getSettings().setPluginState(WebSettings.PluginState.ON);
            popup.getSettings().setSupportMultipleWindows(true);
            popup.setLayoutParams(view.getLayoutParams());
            popup.getSettings().setUserAgentString(popup.getSettings().getUserAgentString().replace("; wv", ""));
            final String[] urlLast = {""};
            popup.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(AUTH) || url.contains(CONSENT)) {
                        Log.d("openWebView", "open vebView 2 " + url);
                        if (popup != null) {
                            popup.loadUrl(url);
                            dialog.openDialog(url, popup);
                        }
                        checkConsent(url);
                    } else {
                        return selectOpenTab(url);
                    }
                    return true;
                }

                private boolean selectOpenTab(String url) {
                    if (openSite.isOpenSupportInBrowser(url)) {
                        openSite.openPrivacyPolicy(url);
                        return destroyWebView();
                    } else if (url.contains("msg_url")) {
                        Log.d("openWebView", "open app " + url);
                        openSite.openApp(url);
                    } else if (url.contains("facebook") || url.contains("twitter") || url.contains("telegram")) {
                        Log.d("openWebView", "open vebView 2.1 " + url);
                        mContainer.addView(popup);
                        popup.loadUrl(url);
                    } else {
                        Log.d("openWebView", "open vebView 1 " + url);
                        mWebViewComments.loadUrl(url);
                        return destroyWebView();
                    }
                    return true;
                }

                private Boolean destroyWebView() {
                    mContainer.removeView(popup);
                    popup.destroy();
                    return false;
                }

                private void checkConsent(String url) {
                    if (urlLast[0].equals(url)) {
                        //   mContainer.removeView(popup);
                        popup.destroy();
                    } else {
                        // mWebViewComments.reload();
                        urlLast[0] = url;
                    }
                }

            });
            popup.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    super.onCloseWindow(window);
                    dialog.close();
                    if (mWebViewComments != null)
                        mWebViewComments.reload();
                    mContainer.removeView(window);
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(popup);
            resultMsg.sendToTarget();

            return true;
        }

        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            uploadMessage = filePathCallback;
            return openPermission();
        }


        private boolean openPermission() {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openPhoto.selectImage(MainActivity.this);
                return true;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                try {
                    openPhoto.selectImage(MainActivity.this);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openPhoto.selectImage(MainActivity.this);
                    return true;
                } else {
                    uploadMessage = null;
                    return false;
                }

            }
        }
    };


}