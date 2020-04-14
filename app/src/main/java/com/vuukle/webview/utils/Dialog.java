package com.vuukle.webview.utils;

import android.Manifest;
import android.app.AlertDialog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vuukle.webview.MainActivity;

public class Dialog {
    private MainActivity context;
    private AlertDialog dialog;
    private Boolean openDialog = true;
    private LinearLayout wrapper;
    private WebView popup;
    private WebView popup1;

    public ValueCallback<Uri[]> uploadMessage;
    public ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE = 1;
    public final static int CAMERA_PERMISSION = 2;
    public OpenSite openSite;
    private OpenPhoto openPhoto = new OpenPhoto();

    public Dialog(MainActivity context) {
        this.context = context;

    }

    public void openDialog(WebView popup) {
        this.popup = popup;
        initLinearLayout();
    }

    public void openDialogOther(String url) {
        openSite = new OpenSite(context);
        popup = new WebView(context);

        popup.loadUrl(url);
        popup.setWebChromeClient(webChromeClient);
        popup.getSettings().setJavaScriptEnabled(true);
        popup.getSettings().setPluginState(WebSettings.PluginState.ON);
        popup.getSettings().setSupportMultipleWindows(false);
        popup.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("mailto:to") || url.contains("mailto:")) {
                    openSite.openEmail(url.replace("%20", " "));
                } else if (url.contains("whatsapp://send") || url.contains("fb-messenger")) {
                    openSite.openWhatsApp(url, popup);
                    openSite.openMessenger(url);
                } else if (url.contains("tg:msg_url"))
                    openSite.openApp(url);
                else
                    popup.loadUrl(url);
                return true;
            }
        });
        initLinearLayout();
    }

    private void initLinearLayout() {
        if (openDialog) {
            openDialog = false;
            wrapper = new LinearLayout(context);
            EditText keyboardHack = new EditText(context);
            keyboardHack.setVisibility(View.GONE);
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.addView(popup, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            wrapper.addView(keyboardHack, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            initDialog(wrapper);
        }
    }

    private void initDialog(LinearLayout wrapper) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setNegativeButton("close", (v, l) -> close());
        builder.setView(wrapper);
        dialog = builder.create();
        dialog.show();

    }

    public void close() {
        if (dialog != null) {
            wrapper.removeView(popup);
            dialog.dismiss();
            openDialog = true;
            popup.destroy();
            popup = null;
        }

    }

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public boolean onCreateWindow(final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            popup1 = new WebView(context);
            popup1.getSettings().setJavaScriptEnabled(true);
            popup1.getSettings().setPluginState(WebSettings.PluginState.ON);
            popup1.getSettings().setSupportMultipleWindows(false);
            popup1.setLayoutParams(view.getLayoutParams());
            popup1.getSettings().setUserAgentString(view.getSettings().getUserAgentString().replace("; wv", ""));
            view.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    popup1.loadUrl(url);
                    return true;
                }

            });
            wrapper.removeView(popup);
            wrapper.addView(popup1, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            initDialog(wrapper);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(view);
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
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openPhoto.selectImage(context);
                return true;
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                try {
                    openPhoto.selectImage(context);
                } catch (Exception e) {
                    Toast.makeText(context, "An error has occurred", Toast.LENGTH_SHORT).show();
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openPhoto.selectImage(context);
                    return true;
                } else {
                    uploadMessage = null;
                    return false;
                }

            }
        }
    };

}