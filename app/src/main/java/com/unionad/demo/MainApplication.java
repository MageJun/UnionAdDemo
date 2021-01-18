package com.unionad.demo;

import android.app.Application;

import com.unionad.sdk.ad.AdClient;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdClient.init(getApplicationContext());
    }
}
