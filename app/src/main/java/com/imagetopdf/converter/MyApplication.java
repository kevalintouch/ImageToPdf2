package com.imagetopdf.converter;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.ads.MobileAds;
import com.imagetopdf.converter.ads.AppOpenManager;


public class MyApplication extends MultiDexApplication {
    private static MyApplication fire_base_app;

    public static synchronized MyApplication getInstance() {
        MyApplication myApplication;
        synchronized (MyApplication.class) {
            myApplication = fire_base_app;
        }
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fire_base_app = this;
        MobileAds.initialize(this, initializationStatus -> Log.e("Mobile Ads :", "Mobile Ads initialize complete!"));
        FastSave.init(getApplicationContext());
        new AppOpenManager(this);
    }


    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
