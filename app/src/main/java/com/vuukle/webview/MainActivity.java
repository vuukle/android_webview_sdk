package com.vuukle.webview;

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
    private final String COMMENTS_URL = "https://cdn.vuukle.com/widgets/index.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&articleId=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1";
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
        setContentView(R.layout.activity_main);
        mWebViewComments = (WebView) findViewById(R.id.activity_main_webview_comments);

        //initialising webview
        configWebView();

        //load url to display in webview
        mWebViewComments.loadUrl(COMMENTS_URL);
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
                    mWebViewComments.loadUrl("javascript:signInUser('" + name + "', '" + email + "')");
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
                mWebViewComments.loadUrl("javascript:signInUser('" + name + "', '" + email + "')");

                //if u use super() it will load url into webview
                return true;
            }
        });
    }
}