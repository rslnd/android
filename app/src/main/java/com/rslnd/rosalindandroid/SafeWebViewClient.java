package com.rslnd.rosalindandroid;

import android.content.Context;
import android.net.http.SslError;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class SafeWebViewClient extends WebViewClient {
    private static final String TAG = "SafeWebClient";
    private Context context;
    private String allowedUrl;

    SafeWebViewClient(Context context, String allowedUrl) {
        this.context = context;
        this.allowedUrl = allowedUrl;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request.getUrl().toString().startsWith(allowedUrl)) {
            return false;
        } else {
            Log.e(TAG, "Disallowed loading " + request.getUrl() + " as it does not start with allowed url " + allowedUrl);
            return true;
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        Log.e(TAG, "Error loading " + request.getUrl() + " " + error.getErrorCode() + " " + error.getDescription());
        showErrorPage(view);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        Log.e(TAG, "HTTP Error loading " + request.getUrl() + " " + errorResponse.getReasonPhrase());
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Log.e(TAG, "SSL Error loading " + error.getUrl());
        showErrorPage(view);
    }

    private void showErrorPage(final WebView webView) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.error);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }

            String html = text.toString();
            byte[] bytes = html.getBytes("UTF-8");
            String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
            webView.loadData(base64, "text/html", "base64");

            Handler handler = new Handler();
            Runnable runnable = new Runnable(){
                public void run() {
                    Log.i(TAG, "Retrying loading " + allowedUrl);
                    webView.loadUrl(allowedUrl);
                }
            };
            handler.postDelayed(runnable, 2000);

        } catch (IOException e) {
            Log.e(TAG, "Error reading error html: " + e.getMessage());
        }
    }
}
