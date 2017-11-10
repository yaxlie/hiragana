package com.mlmg.hiragana;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Marcin on 08.11.2017.
 */

public class HelperApplication extends Application{
    private static HelperApplication mInstance;
    private static Context mAppContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        this.setAppContext(getApplicationContext());
    }

    public static HelperApplication getInstance() {
        return mInstance;
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
}