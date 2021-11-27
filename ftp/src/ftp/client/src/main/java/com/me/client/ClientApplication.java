package com.me.client;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.me.client.common.Constants;
import com.me.client.utils.NetworkUtils;

public class ClientApplication extends Application {
    private static ClientApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        Log.d(Constants.TAG, NetworkUtils.getLocalIpAddress(this));
    }

    public static Context getAppContext()
    {
        return sApplication;
    }
}
