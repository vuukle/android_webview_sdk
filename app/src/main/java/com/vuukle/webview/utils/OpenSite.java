package com.vuukle.webview.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

public class OpenSite {
    private Context context;

    public OpenSite(Context context) {
        this.context = context;
    }

    public void openWhatsApp(String url, WebView view) {
        if (!url.contains("whatsapp://send") && !url.contains("fb-messenger"))
            view.loadUrl(url);
        else if (url.contains("whatsapp://send"))
            openApp("https://api.whatsapp.com" + url.substring(url.indexOf("://") + 2));
    }

    public void openMessenger(String url) {
        if (url.contains("fb-messenger")) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent
                    .putExtra(Intent.EXTRA_TEXT,
                            url.substring(url.indexOf("?link=") + 6));
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.facebook.orca");
            try {
                context.startActivity(sendIntent);
            } catch (android.content.ActivityNotFoundException ex) {
            }
        }
    }

    public void openEmail(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, email.substring(email.indexOf("subject=") + 8, email.indexOf("&body")));
        emailIntent.putExtra(Intent.EXTRA_TEXT, email.substring(email.indexOf("body=") + 5));
        context.startActivity(Intent.createChooser(emailIntent, null));
    }

    public void openApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
