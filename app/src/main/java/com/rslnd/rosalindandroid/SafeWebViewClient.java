package com.rslnd.rosalindandroid;

import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class SafeWebViewClient extends WebViewClient {
    private static final String TAG = "SafeWebClient";

    private String allowedUrl;

    SafeWebViewClient(String allowedUrl) {
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
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        Log.e(TAG, "HTTP Error loading " + request.getUrl() + " " + errorResponse.getReasonPhrase());
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Log.e(TAG, "SSL Error loading " + error.getUrl());
    }



}
