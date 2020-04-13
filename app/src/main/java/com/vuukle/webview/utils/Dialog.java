package com.vuukle.webview.utils;

import android.app.AlertDialog;

import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.vuukle.webview.MainActivity;

public class Dialog {
    private MainActivity context;
    private AlertDialog dialog;
    private Boolean openDialog = true;
    private LinearLayout wrapper;
    private WebView popup;

    public Dialog(MainActivity context) {
        this.context = context;

    }

    public void openDialog(String url, WebView popup) {
        this.popup = popup;
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
            dialog.dismiss();
            openDialog = true;
            wrapper.removeView(popup);
        }

    }
}
