package com.imagetopdf.converter;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.appizona.yehiahd.fastsave.FastSave;


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
        FastSave.init(getApplicationContext());
    }


    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
