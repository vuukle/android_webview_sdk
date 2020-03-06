package com.vuukle.webview.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class OpenSite {
    private Context context;

    public OpenSite(Context context) {
        this.context = context;
    }

    public void openWhatsApp(String url, WebView view) {
        url = decodeUrl(url);
        if (!url.contains("whatsapp://send") && !url.contains("fb-messenger"))
            view.loadUrl(url);
        else if (url.contains("whatsapp://send"))
            openApp("https://api.whatsapp.com" + url.substring(url.indexOf("://") + 2));
    }

    public void openMessenger(String url) {
        url = decodeUrl(url);
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
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.messaging")));

            }
        }
    }

    public void openEmail(String url) {
        url = decodeUrl(url);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, url.substring(url.indexOf("subject=") + 8, url.indexOf("&body")));
        emailIntent.putExtra(Intent.EXTRA_TEXT, url.substring(url.indexOf("body=") + 5));
        try {
            context.startActivity(Intent.createChooser(emailIntent, null));
        } catch (android.content.ActivityNotFoundException ex) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gm")));

        }
    }

    private String decodeUrl(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return url;
        }
        return url;
    }

    public void openApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
