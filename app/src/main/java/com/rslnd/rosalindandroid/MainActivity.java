package com.rslnd.rosalindandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Temporarily set to false for uninstalling
        ensureDeviceOwner(true);

        makeFullscreenInitial();
        setContentView(R.layout.activity_main);
        makeFullscreen();

        SoftKeyboardPanWorkaround.assistActivity(this);

        loadWebView();

        registerReceiver(new PeripheralsReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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

    @Override
    public void onBackPressed() {
        // noop
    }

    private void ensureDeviceOwner(boolean enable) {
        if (enable) {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

            if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
                Log.i(TAG, "This is the device owner");
                setPolicies(true);
            } else {
                Log.e(TAG, "Not device owner, run `adb shell dpm set-device-owner " + getPackageName() + "/.AdminReceiver`");
            }
        } else {
            Log.i(TAG, "Clearing device owner");
            ComponentName devAdminReceiver = new ComponentName(this, AdminReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

            if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
                stopLockTask();
                setPolicies(false);
                dpm.clearDeviceOwnerApp(getPackageName());
                Log.i(TAG, "Cleared device owner");
            }
        }
    }

    private void makeFullscreenInitial() {
        // Restart after crash
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        // Move view up when keyboard is visible
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        );

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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
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

        WebView.setWebContentsDebuggingEnabled(false);

        WebView webView = findViewById(R.id.webview);

        NativeBridge nativeBridge = new NativeBridge(this);
        nativeBridge.setWebView(webView);

        webView.addJavascriptInterface(nativeBridge, "native");

        webView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        webView.setWebViewClient(new SafeWebViewClient(customerUrl));

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);

        webView.loadUrl(customerUrl);
    }

    private void lockScreen() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if(dpm != null) {
            dpm.lockNow();
            Log.i(TAG, "Device locked");
        }
    }

    private void requestTaskLock() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

        if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
            String[] packages = {getPackageName(), "com.android.keyguard"};
            dpm.setLockTaskPackages(deviceAdminReceiver, packages);
            dpm.setCameraDisabled(deviceAdminReceiver, true);
            dpm.setStatusBarDisabled(deviceAdminReceiver, true);

            if (dpm.isLockTaskPermitted(getPackageName())) {
                startLockTask();
                Log.i(TAG, "Task lock active");
            } else {
                stopLockTask();
                Log.e(TAG, "Task lock not permitted");
            }

        } else {
            stopLockTask();
            Log.e(TAG, "Not device owner");
        }
    }

    private void setPolicies(boolean isRestricted) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, isRestricted);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, isRestricted);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, isRestricted);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, isRestricted);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, isRestricted);
    }

    private void setUserRestriction(String restriction, boolean disallow){
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdminReceiver = new ComponentName(this, AdminReceiver.class);

        if (dpm != null) {
            if (disallow) {
                dpm.addUserRestriction(deviceAdminReceiver,restriction);
            } else {
                dpm.clearUserRestriction(deviceAdminReceiver, restriction);
            }
        }
    }
}
