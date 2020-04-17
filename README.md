# WebView guide android

You will be working with our iframe URL’s.

Comment widget iframe looks like this:
```
https://cdn.vuukle.com/widgets/index.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&articleId=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1
```
Required parameters (for comment widget iframe):
<br/>**apiKey** - Your API key (https://docs.vuukle.com/how-to-embed-vuukle-2.0-via-js/)
<br/>**host** - your site host (Exclude http:// or www.)
<br/>**articleId** -unique article ID
<br/>**img** - article image
<br/>**title** - article title
<br/>**url** - article URL (include http:// or www.)
<br/><br/>Emote widget iframe looks like this:
```
https://cdn.vuukle.com/widgets/emotes.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&articleId=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=New%20post%2022&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1
```
Required parameters (for emote widget iframe):
<br/>**apiKey** - Your API key (https://docs.vuukle.com/how-to-embed-vuukle-2.0-via-js/)
<br/>**host** - your site host (Exclude http:// or www.)
<br/>**articleId** -unique article ID
<br/>**img** - article image
<br/>**title** - article title
<br/>**url** - article URL (include http:// or www.)
<br/>If you have any additional options to include, please contact support@vuukle.com

### 1) Create xml resourse:
----------
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout\_width="match\_parent"
    android:layout\_height="match\_parent"
    tools:context="com.example.pc\_5.vuukleweb.MainActivity">
    <WebView
        android:id="@+id/activity\_main\_webview\_comments"
        android:layout\_width="match\_parent"
        android:layout\_height="match\_parent"
        tools:layout\_editor\_absoluteX="8dp"
        tools:layout\_editor\_absoluteY="8dp" />
</RelativeLayout>
```

### 2) Add permission to AndroidManifest.xml:
----------
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

### 3) Getting events from javascript page.
----------
You can listen to events from javascript via console logs. WebChromeClient provides a callback onConsoleMessage.

Example:
```java
//mWebViewComments - your WebView
mWebViewComments.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

      //message
                Log.d("consolejs", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
```

### 4) Passing data to javascript page.
----------
WebView lets you ability to inject your javascript code into page. We can use it for passing data.

Example:
```java
//signInUser(name, email) - function implemented in javascript code on page
mWebViewComments.loadUrl("javascript:signInUser("name", "email")");
```

### 5) Listening on url loading.
----------
We also can override url loading with WebViewClient.

Example:
```java
mWebViewComments.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

     //Clicked url
                Log.d(TAG, "Clicked url: " + url);
                //if u use super() it will load url
                return true;
            }
        });
```
### 6)The "Dialog" file shows a modal window
### 7)The file "OpenPhoto" has the logic of opening a photo
### 8)The "OpenSite" says it's open in or out of the application
### 9)The "ListenerModalWindow" Using this interface, the modal window communicates with the main window.
### 10) Full sample:
----------
```java

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.vuukle.webview.utils.Dialog;
import com.vuukle.webview.utils.OpenPhoto;
import com.vuukle.webview.utils.OpenSite;
import com.vuukle.webview.utils.ListenerModalWindow;

public class MainActivity extends AppCompatActivity implements Reload {
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
    public static final String errorTwitter = "twitter/callback?denied";
    public static final String PRIVACY_POLICY = "https://docs.vuukle.com/";
    public static final String VUUKLE = "https://vuukle.com/";
    public static final String BLOG_VUUKLE = "https://blog.vuukle.com/";
    public static final String AUTH = "auth";
    public static final String CONSENT = "consent";
    private OpenPhoto openPhoto = new OpenPhoto();
    public static final int REQUEST_SELECT_FILE = 1021;
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
                    openSite.openApp(url);
                } else {
                    dialog.openDialogOther(url);
                }
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (CAMERA_PERMISSION == resultCode && requestCode == Activity.RESULT_OK)
            openPhoto.selectImage(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (dialog.uploadMessage == null)
                    return;
                if (intent == null) {
                    Intent intent1 = new Intent();
                    intent1.setData(openPhoto.getImageUri());
                    dialog.uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent1));
                } else
                    dialog.uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                dialog.uploadMessage = null;
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == dialog.uploadMessage)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            dialog.mUploadMessage.onReceiveValue(result);
            dialog.mUploadMessage = null;
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
                    if (popup != null) {
                        if (url.contains(AUTH) || url.contains(CONSENT)) {
                            if (url.contains(errorTwitter))
                                dialog.close();
                            else {
                                popup.loadUrl(url);
                                dialog.openDialog(popup);
                                if (url.contains(CONSENT))
                                    hideKeyboard();
                            }
                        } else {
                            dialog.openDialogOther(url);
                        }
                    }
                    checkConsent(url);
                    return true;
                }

                private void checkConsent(String url) {
                    if (urlLast[0].equals(url)) {
                        dialog.close();
                        popup.destroy();
                    } else {
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

    };

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it

        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void reloadView() {
        mWebViewComments.reload();
    }
}
```
