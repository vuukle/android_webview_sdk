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

### 6) Full sample:
----------
```java
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //URL for loading into WebView
    private final String COMMENTS\_URL = "https://cdn.vuukle.com/widgets/index.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&articleId=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1";

    //WebView
    private WebView mWebViewComments;

    //login name
    String name = "Alex";
    //login email
    String email = "email@test.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialising views
        setContentView(R.layout.activity\_main);
        mWebViewComments = (WebView) findViewById(R.id.activity\_main\_webview\_comments);

        //initialising webview
        configWebView();

        //load url to display in webview
        mWebViewComments.loadUrl(COMMENTS\_URL);
    }
    private void configWebView() {
        //javascript support
        mWebViewComments.getSettings().setJavaScriptEnabled(true);
        //html5 support
        mWebViewComments.getSettings().setDomStorageEnabled(true);

        mWebViewComments.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("consolejs", consoleMessage.message());
                //Listening for console message that contains "Comments initialized!" string
                if(consoleMessage.message().contains("Comments initialized!")) {
                    //signInUser(name, email) - javascript function implemented on a page
                    mWebViewComments.loadUrl("javascript:signInUser(&#39;" + name + "&#39;, &#39;" + email + "&#39;)");
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
        mWebViewComments.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                //Clicked url
                Log.d(TAG, "Clicked url: " + url);

                //Lets signInUser whenever url is clicked just for sample
                mWebViewComments.loadUrl("javascript:signInUser(&#39;" + name + "&#39;, &#39;" + email + "&#39;)");

                //if u use super() it will load url into webview
                return true;
            }
        });
    }
}
```
