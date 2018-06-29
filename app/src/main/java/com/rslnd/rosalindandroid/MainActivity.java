package com.rslnd.rosalindandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ensureDeviceOwner(true);

        makeFullscreenInitial();

        setContentView(R.layout.activity_main);

        loadWebView();
    }

    @Override
    protected void onResume () {
        super.onResume();
        requestTaskLock();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            makeFullscreen();
        }
    }

    // Map volume buttons to lock the screen
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                lockScreen();
                result = true;
                break;

            default:
                result = super.dispatchKeyEvent(event);
                break;
        }

        return result;
    }

    private void ensureDeviceOwner(boolean enable) {
        if (enable) {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

            if (dpm.isDeviceOwnerApp(getPackageName())) {
                Log.i(TAG, "This is the device owner");
            } else {
                Log.e(TAG, "Not device owner, run `adb shell dpm set-device-owner " + getPackageName() + "/.AdminReceiver`");
                System.exit(3);
            }
        } else {
            Log.i(TAG, "Clearing device owner");
            ComponentName devAdminReceiver = new ComponentName(this, AdminReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.clearDeviceOwnerApp(getPackageName());
            Log.i(TAG, "Cleared device owner");
            System.exit(0);
        }
    }

    private void makeFullscreenInitial() {
        // Restart after crash
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        // Prevent Screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        View decorView = getWindow().getDecorView();

        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        makeFullscreen();
                    }
                }
        );

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        makeFullscreen();
    }

    private void makeFullscreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        final String customerUrl = getResources().getString(R.string.customer_url);

        class SafeWebViewClient extends WebViewClient {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().equals(customerUrl)) {
                    return false;
                } else {
                    Log.e(TAG, "Disallowed loading URL " + request.getUrl());
                    return true;
                }
            }
        }

        WebView webView = (WebView) findViewById(R.id.webview);

        webView.setWebViewClient(new SafeWebViewClient());

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(false);

        webView.loadUrl(customerUrl);
    }

    private void lockScreen() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

        if (dpm.isDeviceOwnerApp(getPackageName())) {
            String[] packages = {getPackageName()};
            dpm.setLockTaskPackages(deviceAdminReceiver, packages);
            if (dpm.isLockTaskPermitted(getPackageName())) {
                dpm.lockNow();
            }

        }

    }

    private void requestTaskLock() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

        if (dpm.isDeviceOwnerApp(getPackageName())) {
            String[] packages = {getPackageName()};
            dpm.setLockTaskPackages(deviceAdminReceiver, packages);

            if (dpm.isLockTaskPermitted(getPackageName())) {
                startLockTask();
                Log.i(TAG, "Task lock active");
            } else {
                startLockTask();
                Log.e(TAG, "Task lock not permitted");
            }

        } else {
            startLockTask();
            Log.e(TAG, "Not device owner");
        }
    }
}
