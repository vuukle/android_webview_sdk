package com.vuukle.webview;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //URL for loading into WebView
    private final String COMMENTS_URL = "https://cdntest.vuukle.com/amp.html?apiKey=c7368a34-dac3-4f39-9b7c-b8ac2a2da575&host=smalltester.000webhostapp.com&id=381&img=https://smalltester.000webhostapp.com/wp-content/uploads/2017/10/wallhaven-303371-825x510.jpg&title=Newpost&url=https://smalltester.000webhostapp.com/2017/12/new-post-22#1";
    //login name
    String name = "Alex";
    //login email
    String email = "email@test.com";
    private WebView popup;
    //WebView
    private WebView mWebViewComments;
    private FrameLayout mContainer;

    //Constant
    private static final String AUTH = "auth";
    private static final String CONSENT = "consent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // debug test webView
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }
        //initialising views
        setContentView(R.layout.activity_main);
        mWebViewComments = findViewById(R.id.activity_main_webview_comments);
        mContainer = findViewById(R.id.container);

        //initialising webview
        configWebView();

        //load url to display in webview
        mWebViewComments.loadUrl(COMMENTS_URL);
    }

    @Override
    public void onBackPressed() {
        if (popup != null && popup.getParent() != null) {
            mContainer.removeView(popup);
            popup = null;
            //       mWebViewComments.reload();
        } else {
            mWebViewComments.goBack();
        }
    }

    private void configWebView() {
        //javascript support
        mWebViewComments.getSettings().setJavaScriptEnabled(true);

        mWebViewComments.getSettings().setDomStorageEnabled(true);
        mWebViewComments.getSettings().setSupportMultipleWindows(true);

        mWebViewComments.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("consolejs", consoleMessage.message());
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
                            Log.d("openWebView", "open vebView 2" + url);
                            if(popup!=null)
                                popup.loadUrl(url);
                            checkConsent(url);
                        } else {
                            Log.d("openWebView", "open vebView 1" + url);
                            popup.loadUrl(url);
                        }
                        return true;
                    }

                    private void checkConsent(String url) {
                        if (urlLast[0].equals(url)) {
                            mContainer.removeView(popup);
                            popup.loadUrl("");
                        } else {
                            mWebViewComments.reload();
                            urlLast[0] = url;
                        }
                    }

                });
                popup.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        super.onCloseWindow(window);
                        mContainer.removeView(window);
                    }
                });
                mContainer.addView(popup);

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();

                return true;
            }
        });
        mWebViewComments.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                //Clicked url
                Log.d(TAG, "Clicked url: " + url);

                if (url.contains("mailto:to")) {
                    openEmail(url.replace("%20", " "));
                } else {
                    //Lets signInUser whenever url is clicked just for sample
                    if (!url.contains("whatsapp://send/"))
                        view.loadUrl(url);
                }
                //if u use super() it will load url into webview
                return true;
            }

            private void openEmail(String email) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, email.substring(email.indexOf("subject=") + 8, email.indexOf("&body")));
                emailIntent.putExtra(Intent.EXTRA_TEXT, email.substring(email.indexOf("body=") + 5));
                startActivity(Intent.createChooser(emailIntent, null));
            }
        });
    }
}