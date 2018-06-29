package com.rslnd.rosalindandroid;

import android.app.Application;
import android.content.Context;

public class RosalindApplication extends Application {
    public static RosalindApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static RosalindApplication getInstance() {
        return instance;
    }
}
