package com.rslnd.rosalindandroid;

import android.content.Context;
import android.os.Build;
import android.webkit.JavascriptInterface;

public class NativeBridge {
    private Context context;

    NativeBridge(Context c) {
        context = c;
    }

    @JavascriptInterface
    public String android() {
        return Build.DEVICE;
    }

    @JavascriptInterface
    public void load() {

    }
}
