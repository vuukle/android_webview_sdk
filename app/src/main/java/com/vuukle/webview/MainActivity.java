package com.vuukle.webview;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

import com.vuukle.webview.utils.OpenSite;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    //Constant
    public static final String AUTH = "auth";
    public static final String CONSENT = "consent";

    private static final int REQUEST_SELECT_FILE = 1021;
    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE = 1;

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
        openSite = new OpenSite(this);
        //initialising webview
        configWebView();

        //cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebViewComments, true);
        } else
            CookieManager.getInstance().setAcceptCookie(true);
        //load url to display in webview
        mWebViewComments.loadUrl(COMMENTS_URL);
    }

    @Override
    public void onBackPressed() {
        if (this.popup != null && this.popup.getParent() != null) {
            mContainer.removeView(popup);
            this.popup.destroy();
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

        mWebViewComments.setWebChromeClient(webChromeClient);
        mWebViewComments.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                //Clicked url
                Log.d(TAG, "Clicked url: " + url);

                if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite.openEmail(url.replace("%20", " "));
                } else {
                    //Lets signInUser whenever url is clicked just for sample
                    openSite.openWhatsApp(url, view);
                    openSite.openMessenger(url);
                }
                //if u use super() it will load url into webview
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                if(intent==null){
                    Intent intent1=new Intent();
                    intent1.setData(imageUri);
                    uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent1));
                }else
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
                        if (popup != null)
                            popup.loadUrl(url);
                        checkConsent(url);
                    } else {

                        Log.d("openWebView", "open vebView 1" + url);
                        if (url.contains("msg_url")) {
                            openSite.openApp(url);
                        } else if (url.contains("facebook") || url.contains("twitter") || url.contains("telegram")) {
                            popup.loadUrl(url);
                        } else {
                            mWebViewComments.loadUrl(url);
                            mContainer.removeView(popup);
                            return false;

                        }
                    }
                    return true;
                }

                private void checkConsent(String url) {
                    if (urlLast[0].equals(url)) {
                        mContainer.removeView(popup);
                        popup.destroy();
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

        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            uploadMessage = filePathCallback;
            selectImage();
//           Intent intent = new Intent();
//           intent.setType("image/*");
//           intent.setAction(Intent.ACTION_GET_CONTENT);
//           try
//           {
//               startActivityForResult(intent, REQUEST_SELECT_FILE);
//           } catch (ActivityNotFoundException e)
//           {
//               uploadMessage = null;
//               return false;
//           }
            return true;
        }
    };
    private String FORMAT_TIME = "yyyyMMddHHmmss";
    private String FILE_EXTENSION = ".jpg";
    private String FILE_PROVIDER = "com.vuukle.webview.android.fileprovider";
    private Uri imageUri;
    private File getPictureFile(Context contex) throws IOException {
        String timeStamp = new SimpleDateFormat(FORMAT_TIME).format(new Date());
        String pictureFile = "VUUKLE" + timeStamp;
        File storageDir = contex.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, FILE_EXTENSION, storageDir);
        String pictureFilePath = image.getAbsolutePath();
        return image;
    }

    public void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_your_profile_picture));

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals(getString(R.string.take_photo))) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = null;
                try {
                    photo = getPictureFile(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 imageUri = FileProvider.getUriForFile(
                        MainActivity.this,
                        FILE_PROVIDER,
                        photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        imageUri);
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                try {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                }
            } else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}