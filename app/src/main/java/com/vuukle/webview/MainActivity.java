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
    private final String COMMENTS_URL = "https://test.vuukle.com/widgets/index.aspx?url=http%3A%2F%2Fsmalltester.tech%2Funcategorized%2Fpost-6%2F&apiKey=912921de-df2f-4f07-971c-b47566ef369e&articleId=18&d=false&tags=Uncategorized&title=Post%206&hideArticles=false&custom_text=&filter_tag=undefined&lang=en&ga=&color=&c=1&l_d=false&maxChars=&img=&refHost=test.vuukle.com&host=smalltester.tech&authors=JTVCJTdCJTIybmFtZSUyMjolMjJhZG1pbiUyMiwlMjJlbWFpbCUyMjolMjJsZXN1a2syQGdtYWlsLmNvbSUyMiU3RCU1RA==&commentsToLoad=&emotesEnabled=true&vuukleDiv=vuukle_div&localization_text=&toxicityLimit=&articlesProtocol=http&gr=false&darkMode=false&vv=176";

    //WebView
    private WebView mWebViewComments;

    //login name
    String name = "Ross";
    //login email
    String email = "email@sda";

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